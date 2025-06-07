<?php
/**
 * RentEase API Front Controller
 */

// Simple CORS headers
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Content-Type: application/json');

// Handle OPTIONS requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// Include required files
require_once __DIR__ . '/vendor/autoload.php';
require_once __DIR__ . '/config/Database.php';
require_once __DIR__ . '/config/Config.php';
require_once __DIR__ . '/config/Routes.php';
require_once __DIR__ . '/services/ResponseService.php';
require_once __DIR__ . '/controllers/Controller.php';
require_once __DIR__ . '/controllers/AuthController.php';
require_once __DIR__ . '/controllers/UserController.php';
require_once __DIR__ . '/controllers/PropertyController.php';
require_once __DIR__ . '/controllers/LandlordController.php';
require_once __DIR__ . '/models/User.php';
require_once __DIR__ . '/models/Property.php';

// Get the requested path and method
$path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);
$method = $_SERVER['REQUEST_METHOD'];

// Route the request
$route = Routes::match($path, $method);

if (!$route) {
    header('HTTP/1.1 404 Not Found');
    echo json_encode(['error' => 'Endpoint not found', 'path' => $path, 'method' => $method]);
    exit;
}

// Initialize database and services first
try {
    $database = new Database();
    $db = $database->getConnection();
    $responseService = new ResponseService();
} catch (Exception $e) {
    header('HTTP/1.1 500 Internal Server Error');
    echo json_encode(['error' => 'Database connection failed']);
    exit;
}

// Simple auth check (simplified for academic project)
$user_data = null;

// Always parse Authorization header for demo tokens (controllers may need user data for authorization)
$auth_header = null;

// Try multiple ways to get the Authorization header
if (isset($_SERVER['HTTP_AUTHORIZATION'])) {
    $auth_header = $_SERVER['HTTP_AUTHORIZATION'];
} elseif (function_exists('getallheaders')) {
    $headers = getallheaders();
    if (isset($headers['Authorization'])) {
        $auth_header = $headers['Authorization'];
    }
} elseif (isset($_SERVER['REDIRECT_HTTP_AUTHORIZATION'])) {
    $auth_header = $_SERVER['REDIRECT_HTTP_AUTHORIZATION'];
}

if ($auth_header && preg_match('/Bearer\s+(.*)$/i', $auth_header, $matches)) {
    $token = $matches[1];
    // Parse demo token format: demo-token-{user_id} or demo-token-{user_id}-{timestamp}
    if (preg_match('/^demo-token-(\d+)/', $token, $token_matches)) {
        $user_id = (int)$token_matches[1];

        // Get user data from database for authorization
        try {
            $stmt = $db->prepare("SELECT id, username, email, user_type FROM users WHERE id = ?");
            $stmt->execute([$user_id]);
            $user_data = $stmt->fetch(PDO::FETCH_ASSOC);
        } catch (Exception $e) {
            // Continue without user data if database error
        }
    }
}

// Check if route requires authentication
if ($route['auth'] && !$user_data) {
    header('HTTP/1.1 401 Unauthorized');
    echo json_encode(['error' => 'Authentication required']);
    exit;
}

// Prepare request data
$pathParts = explode('/', trim($path, '/'));

// Determine action based on route and path
$action = '';
if ($route['method'] === 'getAll') {
    $action = ''; // Empty action for getAll to work with parent Controller logic
} elseif ($route['method'] === 'getOne') {
    $action = $route['params']['id'] ?? '';
} elseif (in_array($route['method'], ['update', 'delete'])) {
    // For PUT/PATCH/DELETE requests, the action should be the ID parameter
    $action = $route['params']['id'] ?? '';
} else {
    $action = $route['method'];
}

$requestData = [
    'method' => $method,
    'path' => $path,
    'path_parts' => $pathParts,
    'params' => $route['params'],
    'route_params' => $route['params'], // Add route_params for easier access
    'query' => $_GET,
    'body' => json_decode(file_get_contents('php://input'), true) ?: [],
    'action' => $action,
    'user' => $user_data // Add user data for authorization
];

// Call the controller
$controllerName = $route['controller'];

try {
    $controller = new $controllerName($db, $responseService, $requestData);
    $controller->processRequest();
    // Response is handled by the ResponseService, no need to echo here
} catch (Exception $e) {
    header('HTTP/1.1 500 Internal Server Error');
    echo json_encode(['error' => 'Internal server error']);
}
