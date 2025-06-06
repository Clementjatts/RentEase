<?php
/**
 * RentEase API - Main Router
 *
 * Single entry point for all API requests.
 * Handles routing to appropriate controllers, authentication, and error responses.
 */

// Disable displaying PHP errors directly
ini_set('display_errors', 'Off');
ini_set('display_startup_errors', 'Off');


error_reporting(E_ALL);

// Custom error handler to return JSON responses instead of PHP errors
function json_error_handler($errno, $errstr, $errfile, $errline) {
    // Log the error
    error_log("PHP Error: [$errno] $errstr in $errfile on line $errline");

    // Return a JSON error response
    header('Content-Type: application/json');
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'Internal server error',
        'success' => false
    ]);
    exit();
}

// Register the custom error handler
set_error_handler('json_error_handler', E_ALL);

// Set headers for all responses
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Origin, Content-Type, Authorization, X-Requested-With');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
// Load Composer autoloader first
require_once __DIR__ . "/vendor/autoload.php";
    exit();
}

// Load configuration
require_once __DIR__ . '/config/Config.php';
require_once __DIR__ . '/config/Database.php';
require_once __DIR__ . '/config/Routes.php';

// Load utility classes
require_once __DIR__ . '/services/ResponseService.php';

// Initialize services
$database = new Database();
$db = $database->getConnection();
$response = new ResponseService();

// Parse request
$request_uri = $_SERVER['REQUEST_URI'];
$request_method = $_SERVER['REQUEST_METHOD'];
$path = parse_url($request_uri, PHP_URL_PATH);

// Remove base path if present (adjust as needed based on server configuration)
$base_path = '/backend/api';
if (strpos($path, $base_path) === 0) {
    $path = substr($path, strlen($base_path));
}

// Parse query string parameters
$query_params = [];
parse_str(parse_url($request_uri, PHP_URL_QUERY) ?? '', $query_params);

// Get request body for non-GET requests
$body = null;
if ($request_method !== 'GET') {
    // For POST requests, especially with file uploads, data might be in $_POST
    if (!empty($_POST)) {
        $body = $_POST;
    } else {
        // Fallback for other content types like application/json
        $json_body = file_get_contents('php://input');
        if ($json_body) {
            $body = json_decode($json_body, true);
        }
    }
}

// Prepare request data for controllers
$request_data = [
    'method' => $request_method,
    'path' => $path,
    'path_parts' => explode('/', trim($path, '/')),
    'query' => $query_params,
    'body' => $body,
    'headers' => getallheaders()
];

try {
    // Match the route
    $matched_route = Routes::match($path, $request_method);

    if (!$matched_route) {
        $response->notFound("Endpoint not found: $path");
        exit();
    }

    $controller_name = $matched_route['controller'];
    $action_method = $matched_route['method'];
    $route_params = $matched_route['params'];

    // Update request_data with matched action and route parameters
    // For base Controller compatibility:
    // - getAll should have empty action
    // - getOne, update, delete should have the ID as action
    if ($action_method === 'getAll') {
        $request_data['action'] = '';
    } elseif (in_array($action_method, ['getOne', 'update', 'delete']) && isset($route_params['id'])) {
        $request_data['action'] = $route_params['id'];
    } else {
        $request_data['action'] = $action_method;
    }
    $request_data['route_params'] = $route_params;

    // Extract user information from Authorization header (simplified for academic project)
    $headers = getallheaders();
    if (isset($headers['Authorization'])) {
        $auth_header = $headers['Authorization'];
        if (preg_match('/Bearer\s+(.*)$/i', $auth_header, $matches)) {
            $token = $matches[1];
            // Parse demo token format: demo-token-{user_id}
            if (preg_match('/demo-token-(\d+)/', $token, $token_matches)) {
                $user_id = (int)$token_matches[1];
                // Get user from database
                $user_query = "SELECT id, username, email, user_type FROM users WHERE id = ?";
                $user_stmt = $db->prepare($user_query);
                $user_stmt->execute([$user_id]);
                $user = $user_stmt->fetch(PDO::FETCH_ASSOC);

                if ($user) {
                    $request_data['user'] = $user;
                }
            }
        }
    }

    // Check authentication requirement
    if ($matched_route['auth'] && !isset($request_data['user'])) {
        $response->unauthorized('Authentication required');
        exit();
    }

    // Check if controller file exists
    if (!file_exists(__DIR__ . "/controllers/{$controller_name}.php")) {
        $response->notFound("Controller not found for path: $path");
        exit();
    }

    // Load the base Controller class first
    require_once __DIR__ . "/controllers/Controller.php";

    // Load models (simplified schema)
    require_once __DIR__ . "/models/Property.php";
    require_once __DIR__ . "/models/User.php";

    // Load the specific controller class
    require_once __DIR__ . "/controllers/{$controller_name}.php";

    // Initialize controller
    $controller = new $controller_name($db, $response, $request_data);

    // Process the request through the controller
    $result = $controller->processRequest();

    // If the controller returns an array, convert it to a JSON response
    if (is_array($result)) {
        header('Content-Type: application/json');
        echo json_encode($result);
    }

} catch (Exception $e) {
    // Log the error
    $logger->logError("Exception: " . $e->getMessage(), [
        'file' => $e->getFile(),
        'line' => $e->getLine(),
        'trace' => $e->getTraceAsString()
    ]);

    // Return a generic error to avoid exposing implementation details
    // Make sure the response includes the 'success' field expected by the Android app
    header('Content-Type: application/json');
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'Internal server error',
        'success' => false
    ]);
}

// Register a shutdown function to catch fatal errors
register_shutdown_function(function() {
    $error = error_get_last();
    if ($error && in_array($error['type'], [E_ERROR, E_PARSE, E_CORE_ERROR, E_COMPILE_ERROR])) {
        // Log the fatal error
        error_log("Fatal Error: [{$error['type']}] {$error['message']} in {$error['file']} on line {$error['line']}");

        // Return a JSON error response
        header('Content-Type: application/json');
        http_response_code(500);
        echo json_encode([
            'status' => 'error',
            'message' => 'Internal server error',
            'success' => false
        ]);
    }
});
