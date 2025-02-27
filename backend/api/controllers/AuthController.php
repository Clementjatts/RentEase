<?php
/**
 * Auth Controller
 *
 * Handles authentication-related API requests
 */

// Explicitly require the User model
require_once __DIR__ . '/../models/User.php';

class AuthController {
    private $db;
    private $service;
    private $request;
    private $user;
    
    /**
     * Constructor
     * 
     * @param PDO $db Database connection
     * @param ResponseService $service Response service
     * @param array $request Request data
     */
    public function __construct($db, $service, $request) {
        $this->db = $db;
        $this->service = $service;
        $this->request = $request;
        $this->user = new User($db);
    }
    
    /**
     * Login a user
     *
     * @return void
     */
    public function login() {
        // Validate request
        if (!isset($this->request['email']) || !isset($this->request['password'])) {
            return $this->service->badRequest('Username and password are required');
        }
        
        // Extract credentials
        $email = $this->request['email'];
        $password = $this->request['password'];
        
        // Authenticate user
        $user = $this->user->findByEmail($email);
        
        if (!$user) {
            return $this->service->unauthorized('Invalid credentials');
        }
        
        // Verify password
        if (!password_verify($password, $user['password'])) {
            return $this->service->unauthorized('Invalid credentials');
        }
        
        // Generate token
        $token = $this->generateToken($user['id']);
        
        // Return response
        return $this->service->success('Login successful', [
            'token' => $token,
            'user' => [
                'id' => $user['id'],
                'name' => $user['name'],
                'email' => $user['email'],
                'role' => $user['role']
            ]
        ]);
    }
    
    /**
     * Register a new user
     *
     * @return void
     */
    public function register() {
        // Validate request
        if (!isset($this->request['name']) || !isset($this->request['email']) || !isset($this->request['password'])) {
            return $this->service->badRequest('Username, email, and password are required');
        }
        
        // Check if passwords match
        if (isset($this->request['confirm_password']) && $this->request['password'] !== $this->request['confirm_password']) {
            return $this->service->badRequest('Passwords do not match');
        }
        
        // Check if user already exists
        $existing = $this->user->findByEmail($this->request['email']);
        if ($existing) {
            return $this->service->conflict('Username or email already exists');
        }
        
        // Create user
        $user = [
            'name' => $this->request['name'],
            'email' => $this->request['email'],
            'password' => password_hash($this->request['password'], PASSWORD_DEFAULT),
            'role' => 'user' // Default role
        ];
        
        $id = $this->user->create($user);
        
        if (!$id) {
            return $this->service->serverError('Failed to create user');
        }
        
        // Generate token
        $token = $this->generateToken($id);
        
        // Return response
        return $this->service->created('User registered successfully', [
            'token' => $token,
            'user' => [
                'id' => $id,
                'name' => $user['name'],
                'email' => $user['email'],
                'role' => $user['role']
            ]
        ]);
    }
    
    /**
     * Get the current authenticated user
     *
     * @param array $user Authenticated user data
     * @return void
     */
    public function getCurrentUser($user) {
        if (!$user) {
            return $this->service->unauthorized('User not found');
        }
        
        return $this->service->success('User found', [
            'user' => [
                'id' => $user['id'],
                'name' => $user['name'],
                'email' => $user['email'],
                'role' => $user['role']
            ]
        ]);
    }
    
    /**
     * Generate a JWT token
     *
     * @param int $userId User ID
     * @return string JWT token
     */
    private function generateToken($userId) {
        $issuedAt = time();
        $expirationTime = $issuedAt + 60 * 60 * 24; // 24 hours
        
        $payload = [
            'iat' => $issuedAt,
            'exp' => $expirationTime,
            'user_id' => $userId
        ];
        
        // Use the JWT_SECRET from environment or a default value
        $secret = getenv('JWT_SECRET') ?: 'default_secret_key';
        
        // Create token with base64 encoding (simplified JWT implementation)
        $header = base64_encode(json_encode(['alg' => 'HS256', 'typ' => 'JWT']));
        $payload = base64_encode(json_encode($payload));
        $signature = base64_encode(hash_hmac('sha256', "$header.$payload", $secret, true));
        
        return "$header.$payload.$signature";
    }
}
