<?php
/**
 * Routes Configuration
 *
 * Defines all API routes and their handlers
 */
class Routes {
    // Route definitions with controllers and methods
    private static $routes = [
        // Property routes
        ['method' => 'GET', 'path' => '/properties', 'controller' => 'PropertyController', 'action' => 'getAll', 'auth' => false],
        ['method' => 'GET', 'path' => '/properties/{id}', 'controller' => 'PropertyController', 'action' => 'getOne', 'auth' => false],
        ['method' => 'POST', 'path' => '/properties', 'controller' => 'PropertyController', 'action' => 'create', 'auth' => false],
        ['method' => 'PUT', 'path' => '/properties/{id}', 'controller' => 'PropertyController', 'action' => 'update', 'auth' => false],
        ['method' => 'DELETE', 'path' => '/properties/{id}', 'controller' => 'PropertyController', 'action' => 'delete', 'auth' => false],

        // Property image routes
        ['method' => 'GET', 'path' => '/properties/{id}/images', 'controller' => 'PropertyController', 'action' => 'getImages', 'auth' => false],
        ['method' => 'POST', 'path' => '/properties/upload-image', 'controller' => 'PropertyController', 'action' => 'uploadImage', 'auth' => false],
        ['method' => 'DELETE', 'path' => '/properties/images/{id}', 'controller' => 'PropertyController', 'action' => 'deleteImage', 'auth' => false],

        // User routes
        ['method' => 'GET', 'path' => '/users', 'controller' => 'UserController', 'action' => 'getAll', 'auth' => false],
        ['method' => 'GET', 'path' => '/users/{id}', 'controller' => 'UserController', 'action' => 'getOne', 'auth' => false],
        ['method' => 'PUT', 'path' => '/users/{id}', 'controller' => 'UserController', 'action' => 'update', 'auth' => false],

        // Authentication routes
        ['method' => 'POST', 'path' => '/auth/login', 'controller' => 'AuthController', 'action' => 'login', 'auth' => false],
        ['method' => 'POST', 'path' => '/auth/register', 'controller' => 'AuthController', 'action' => 'register', 'auth' => false],
        ['method' => 'POST', 'path' => '/auth/password', 'controller' => 'AuthController', 'action' => 'changePassword', 'auth' => true],
        ['method' => 'GET', 'path' => '/auth/me', 'controller' => 'AuthController', 'action' => 'getCurrentUser', 'auth' => false],

        // Landlord routes
        ['method' => 'GET', 'path' => '/landlords', 'controller' => 'LandlordController', 'action' => 'getAll', 'auth' => false],
        ['method' => 'GET', 'path' => '/landlords/by-user', 'controller' => 'LandlordController', 'action' => 'getByUserId', 'auth' => true],
        ['method' => 'GET', 'path' => '/landlords/{id}', 'controller' => 'LandlordController', 'action' => 'getOne', 'auth' => false],
        ['method' => 'POST', 'path' => '/landlords', 'controller' => 'LandlordController', 'action' => 'create', 'auth' => false],
        ['method' => 'PUT', 'path' => '/landlords/{id}', 'controller' => 'LandlordController', 'action' => 'update', 'auth' => false],
        ['method' => 'DELETE', 'path' => '/landlords/{id}', 'controller' => 'LandlordController', 'action' => 'delete', 'auth' => false],
    ];

    /**
     * Match a request path and method to a route
     *
     * @param string $path The request path
     * @param string $method The HTTP method
     * @return array|null The matched route or null if no match
     */
    public static function match($path, $method) {
        $path = trim($path, '/');
        $path_segments = explode('/', $path);

        foreach (self::$routes as $route) {
            if ($route['method'] !== $method) {
                continue; // Skip if HTTP method doesn't match
            }

            $route_path = trim($route['path'], '/');
            $route_segments = explode('/', $route_path);

            // Different number of segments means no match
            if (count($path_segments) !== count($route_segments)) {
                continue;
            }

            $params = [];
            $matches = true;

            // Compare each segment
            for ($i = 0; $i < count($route_segments); $i++) {
                // Check if this is a parameter segment {param}
                if (preg_match('/^\{([a-zA-Z0-9_]+)\}$/', $route_segments[$i], $matches_param)) {
                    // This is a parameter, store its value
                    $param_name = $matches_param[1];
                    $params[$param_name] = $path_segments[$i];
                    continue;
                }

                // Regular segment, must match exactly
                if ($route_segments[$i] !== $path_segments[$i]) {
                    $matches = false;
                    break;
                }
            }

            if ($matches) {
                return [
                    'controller' => $route['controller'],
                    'method' => $route['action'],
                    'params' => $params,
                    'auth' => $route['auth']
                ];
            }
        }

        return null; // No match found
    }
}
