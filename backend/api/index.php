<?php
/**
 * RentEase API - Main Router
 * 
 * Single entry point for all API requests.
 * Handles routing to appropriate controllers, authentication, and error responses.
 */

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
require_once __DIR__ . '/utils/Logger.php';
require_once __DIR__ . '/services/ResponseService.php';

// Initialize services
$database = new Database();
$db = $database->getConnection();
$response = new ResponseService();
$logger = new Logger();

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
    $body = json_decode(file_get_contents('php://input'), true);
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

// Log incoming request
$logger->logRequestFromArray($request_data);

try {
    // Determine which controller to use based on the first path segment
    $controller_name = isset($request_data['path_parts'][0]) ? ucfirst($request_data['path_parts'][0]) . 'Controller' : null;
    
    // Special case for auth routes
    if ($controller_name === 'AuthController' || (isset($request_data['path_parts'][0]) && $request_data['path_parts'][0] === 'auth')) {
        $controller_name = 'AuthController';
    } elseif (!$controller_name || !file_exists(__DIR__ . "/controllers/{$controller_name}.php")) {
        // Default to a 404 response if controller doesn't exist
        $response->notFound("Endpoint not found: $path");
        exit();
    }
    
    // Load the controller class
    require_once __DIR__ . "/controllers/{$controller_name}.php";
    
    // Check if authentication is required for this path
    require_once __DIR__ . '/middleware/AuthMiddleware.php';
    $auth = new AuthMiddleware($db);
    
    $authenticated_user = null;
    $auth_header = isset($request_data['headers']['Authorization']) ? $request_data['headers']['Authorization'] : null;
    
    if ($auth_header) {
        $authenticated_user = $auth->authenticate();
        if ($authenticated_user) {
            $request_data['user'] = $authenticated_user;
        }
    }
    
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
    $response->serverError("Internal server error");
}
