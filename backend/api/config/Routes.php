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
        ['method' => 'GET', 'path' => '/properties', 'controller' => 'PropertyController', 'action' => 'listAll', 'auth' => false],
        ['method' => 'GET', 'path' => '/properties/{id}', 'controller' => 'PropertyController', 'action' => 'getOne', 'auth' => false],
        ['method' => 'POST', 'path' => '/properties', 'controller' => 'PropertyController', 'action' => 'create', 'auth' => true],
        ['method' => 'PUT', 'path' => '/properties/{id}', 'controller' => 'PropertyController', 'action' => 'update', 'auth' => true],
        ['method' => 'DELETE', 'path' => '/properties/{id}', 'controller' => 'PropertyController', 'action' => 'delete', 'auth' => true],
        ['method' => 'GET', 'path' => '/properties/search', 'controller' => 'PropertyController', 'action' => 'search', 'auth' => false],
        
        // Favorite routes
        ['method' => 'GET', 'path' => '/favorites', 'controller' => 'FavoriteController', 'action' => 'listAll', 'auth' => true],
        ['method' => 'GET', 'path' => '/favorites/{id}', 'controller' => 'FavoriteController', 'action' => 'getOne', 'auth' => true],
        ['method' => 'POST', 'path' => '/favorites', 'controller' => 'FavoriteController', 'action' => 'create', 'auth' => true],
        ['method' => 'DELETE', 'path' => '/favorites/{id}', 'controller' => 'FavoriteController', 'action' => 'delete', 'auth' => true],
        ['method' => 'GET', 'path' => '/favorites/user/{user_id}', 'controller' => 'FavoriteController', 'action' => 'getByUser', 'auth' => true],
        ['method' => 'GET', 'path' => '/favorites/property/{property_id}', 'controller' => 'FavoriteController', 'action' => 'getByProperty', 'auth' => false],
        
        // Request routes
        ['method' => 'GET', 'path' => '/requests', 'controller' => 'RequestController', 'action' => 'listAll', 'auth' => true],
        ['method' => 'GET', 'path' => '/requests/{id}', 'controller' => 'RequestController', 'action' => 'getOne', 'auth' => true],
        ['method' => 'POST', 'path' => '/requests', 'controller' => 'RequestController', 'action' => 'create', 'auth' => true],
        ['method' => 'PUT', 'path' => '/requests/{id}', 'controller' => 'RequestController', 'action' => 'update', 'auth' => true],
        ['method' => 'DELETE', 'path' => '/requests/{id}', 'controller' => 'RequestController', 'action' => 'delete', 'auth' => true],
        ['method' => 'GET', 'path' => '/requests/user/{user_id}', 'controller' => 'RequestController', 'action' => 'getByUser', 'auth' => true],
        ['method' => 'GET', 'path' => '/requests/property/{property_id}', 'controller' => 'RequestController', 'action' => 'getByProperty', 'auth' => true],
        
        // User routes
        ['method' => 'GET', 'path' => '/users', 'controller' => 'UserController', 'action' => 'listAll', 'auth' => true],
        ['method' => 'GET', 'path' => '/users/{id}', 'controller' => 'UserController', 'action' => 'getOne', 'auth' => true],
        ['method' => 'PUT', 'path' => '/users/{id}', 'controller' => 'UserController', 'action' => 'update', 'auth' => true],
        
        // Authentication routes
        ['method' => 'POST', 'path' => '/auth/login', 'controller' => 'AuthController', 'action' => 'login', 'auth' => false],
        ['method' => 'POST', 'path' => '/auth/register', 'controller' => 'AuthController', 'action' => 'register', 'auth' => false],
        ['method' => 'GET', 'path' => '/auth/me', 'controller' => 'AuthController', 'action' => 'getCurrentUser', 'auth' => true],
        
        // Landlord routes
        ['method' => 'GET', 'path' => '/landlords', 'controller' => 'LandlordController', 'action' => 'listAll', 'auth' => false],
        ['method' => 'GET', 'path' => '/landlords/{id}', 'controller' => 'LandlordController', 'action' => 'getOne', 'auth' => false],
        ['method' => 'POST', 'path' => '/landlords', 'controller' => 'LandlordController', 'action' => 'create', 'auth' => true],
        ['method' => 'PUT', 'path' => '/landlords/{id}', 'controller' => 'LandlordController', 'action' => 'update', 'auth' => true],
        ['method' => 'DELETE', 'path' => '/landlords/{id}', 'controller' => 'LandlordController', 'action' => 'delete', 'auth' => true],
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
            
            // Check for special case of route with query parameters (e.g., /properties/search)
            if (count($path_segments) === 1 && count($route_segments) === 2 && $path_segments[0] === $route_segments[0] && $route_segments[1] === 'search') {
                return [
                    'controller' => $route['controller'],
                    'method' => $route['action'],
                    'params' => $_GET,
                    'auth' => $route['auth']
                ];
            }
            
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
