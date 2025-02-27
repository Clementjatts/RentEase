<?php
/**
 * Authentication Middleware
 * 
 * Verifies user authentication via JWT tokens
 */
class AuthMiddleware {
    private $db;
    
    /**
     * Constructor
     * 
     * @param PDO $db Database connection
     */
    public function __construct($db = null) {
        $this->db = $db;
    }
    
    /**
     * Authenticate a request
     * 
     * @return array|false User data if authenticated, false otherwise
     */
    public function authenticate() {
        // Get Authorization header
        $auth_header = isset($_SERVER['HTTP_AUTHORIZATION']) ? $_SERVER['HTTP_AUTHORIZATION'] : '';
        
        // Check if bearer token is present
        if (!preg_match('/Bearer\s(\S+)/', $auth_header, $matches)) {
            return false;
        }
        
        $token = $matches[1];
        
        // Verify token
        try {
            // Load JWT and Config classes
            require_once __DIR__ . '/../utils/JWT.php';
            require_once __DIR__ . '/../config/Config.php';
            
            // Get JWT configuration
            $jwt_config = Config::getJwtConfig();
            
            // Decode the token
            $decoded = JWT::decode($token, $jwt_config['secret_key'], $jwt_config['algorithm']);
            
            if (!$decoded || !isset($decoded['data']) || !isset($decoded['data']['id'])) {
                return false;
            }
            
            $user_id = $decoded['data']['id'];
            
            // Get user from database
            $query = "SELECT id, username, email, role FROM users WHERE id = ?";
            $stmt = $this->db->prepare($query);
            $stmt->execute([$user_id]);
            $user = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if (!$user) {
                return false;
            }
            
            return $user;
        } catch (Exception $e) {
            // Log the error
            error_log('JWT Error: ' . $e->getMessage());
            return false;
        }
    }
    
    /**
     * Generate a token for a user
     * 
     * @param array $user User data
     * @return string JWT token
     */
    public function generateToken($user) {
        // Load JWT and Config classes
        require_once __DIR__ . '/../utils/JWT.php';
        require_once __DIR__ . '/../config/Config.php';
        
        // Get JWT configuration
        $jwt_config = Config::getJwtConfig();
        
        $issued_at = time();
        $expiration = $issued_at + $jwt_config['expiration'];
        
        $payload = [
            'iat' => $issued_at,
            'exp' => $expiration,
            'data' => [
                'id' => $user['id'],
                'username' => $user['username'],
                'email' => $user['email'],
                'role' => $user['role']
            ]
        ];
        
        // JWT is created using the HMAC-SHA256 algorithm
        return JWT::encode($payload, $jwt_config['secret_key'], 'HS256');
    }
}
