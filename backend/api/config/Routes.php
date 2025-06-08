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

        // User routes (enhanced to support landlord functionality)
        ['method' => 'GET', 'path' => '/users', 'controller' => 'UserController', 'action' => 'getAll', 'auth' => false],
        ['method' => 'GET', 'path' => '/users/{id}', 'controller' => 'UserController', 'action' => 'getOne', 'auth' => false],
        ['method' => 'PUT', 'path' => '/users/{id}', 'controller' => 'UserController', 'action' => 'update', 'auth' => false],
        ['method' => 'POST', 'path' => '/users', 'controller' => 'UserController', 'action' => 'create', 'auth' => false],
        ['method' => 'DELETE', 'path' => '/users/{id}', 'controller' => 'UserController', 'action' => 'delete', 'auth' => false],

        // Landlord routes (consolidated into UserController)
        ['method' => 'GET', 'path' => '/users/landlords', 'controller' => 'UserController', 'action' => 'landlords', 'auth' => false],
        ['method' => 'GET', 'path' => '/users/landlord/by-user', 'controller' => 'UserController', 'action' => 'landlord-by-user', 'auth' => true],
        ['method' => 'DELETE', 'path' => '/users/landlord/{id}', 'controller' => 'UserController', 'action' => 'delete-landlord', 'auth' => false],
        ['method' => 'GET', 'path' => '/users/landlord/{id}/properties', 'controller' => 'UserController', 'action' => 'landlord-properties', 'auth' => false],

        // Backward compatibility routes (redirect to new endpoints)
        ['method' => 'GET', 'path' => '/landlords', 'controller' => 'UserController', 'action' => 'landlords', 'auth' => false],
        ['method' => 'GET', 'path' => '/landlords/by-user', 'controller' => 'UserController', 'action' => 'landlord-by-user', 'auth' => true],
        ['method' => 'GET', 'path' => '/landlords/{id}', 'controller' => 'UserController', 'action' => 'getOne', 'auth' => false],
        ['method' => 'POST', 'path' => '/landlords', 'controller' => 'UserController', 'action' => 'create', 'auth' => false],
        ['method' => 'PUT', 'path' => '/landlords/{id}', 'controller' => 'UserController', 'action' => 'update', 'auth' => false],
        ['method' => 'DELETE', 'path' => '/landlords/{id}', 'controller' => 'UserController', 'action' => 'delete-landlord', 'auth' => false],

        // Request routes
        ['method' => 'POST', 'path' => '/requests', 'controller' => 'RequestController', 'action' => 'create', 'auth' => false],
        ['method' => 'GET', 'path' => '/requests/landlord/{landlordId}', 'controller' => 'RequestController', 'action' => 'getByLandlord', 'auth' => false],
        ['method' => 'PATCH', 'path' => '/requests/{id}/read', 'controller' => 'RequestController', 'action' => 'markAsRead', 'auth' => false],
        ['method' => 'GET', 'path' => '/requests/landlord/{landlordId}/unread-count', 'controller' => 'RequestController', 'action' => 'getUnreadCount', 'auth' => false],

        // Authentication routes
        ['method' => 'POST', 'path' => '/auth/login', 'controller' => 'AuthController', 'action' => 'login', 'auth' => false],
        ['method' => 'POST', 'path' => '/auth/register', 'controller' => 'AuthController', 'action' => 'register', 'auth' => false],
        ['method' => 'POST', 'path' => '/auth/password', 'controller' => 'AuthController', 'action' => 'changePassword', 'auth' => true],
        ['method' => 'GET', 'path' => '/auth/me', 'controller' => 'AuthController', 'action' => 'getCurrentUser', 'auth' => false],
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
