<?php
/**
 * Authentication helper functions
 */

class AuthHelper {
    private $db;
    
    public function __construct($db) {
        $this->db = $db;
    }
    
    /**
     * Get user from the Authorization header
     * Tries both users and admins tables
     */
    public function getUserFromAuth() {
        // Check for Authorization header
        $auth_header = isset($_SERVER['HTTP_AUTHORIZATION']) ? $_SERVER['HTTP_AUTHORIZATION'] : '';
        if (!$auth_header) {
            // Check if PHP-CGI
            $auth_header = isset($_SERVER['REDIRECT_HTTP_AUTHORIZATION']) ? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] : '';
        }
        
        if (!$auth_header) {
            return null;
        }
        
        if (strpos($auth_header, 'Basic ') === 0) {
            $credentials = explode(':', base64_decode(substr($auth_header, 6)));
            if (count($credentials) != 2) {
                return null;
            }
            
            list($username, $password) = $credentials;
            
            // First try the users table
            $query = "SELECT * FROM users WHERE username = :username AND password = :password";
            $stmt = $this->db->prepare($query);
            $stmt->bindParam(':username', $username);
            $stmt->bindParam(':password', $password);
            $stmt->execute();
            
            if ($stmt->rowCount() > 0) {
                return $stmt->fetch(PDO::FETCH_ASSOC);
            }
            
            // If not found, try the admins table (legacy)
            $query = "SELECT id, username FROM admins WHERE username = :username AND password = :password";
            $stmt = $this->db->prepare($query);
            $stmt->bindParam(':username', $username);
            $stmt->bindParam(':password', $password);
            $stmt->execute();
            
            if ($stmt->rowCount() > 0) {
                $admin = $stmt->fetch(PDO::FETCH_ASSOC);
                // Convert to user format
                return [
                    'id' => $admin['id'],
                    'username' => $admin['username'],
                    'email' => 'admin@rentease.com',
                    'user_type' => 'ADMIN',
                    'full_name' => 'Admin User',
                    'phone' => '',
                    'created_at' => date('Y-m-d H:i:s')
                ];
            }
        } elseif (strpos($auth_header, 'Bearer ') === 0) {
            // For future token-based authentication
            $token = substr($auth_header, 7);
            // You would validate the token here
        }
        
        return null;
    }
    
    /**
     * Require authentication or exit with 401
     */
    public function requireAuth() {
        $user = $this->getUserFromAuth();
        
        if (!$user) {
            http_response_code(401);
            echo json_encode([
                'message' => 'Unauthorized'
            ]);
            exit;
        }
        
        return $user;
    }
    
    /**
     * Require admin authentication or exit with 403
     */
    public function requireAdmin() {
        $user = $this->requireAuth();
        
        if ($user['user_type'] !== 'ADMIN') {
            http_response_code(403);
            echo json_encode([
                'message' => 'Access denied. Admin privileges required.'
            ]);
            exit;
        }
        
        return $user;
    }
}
