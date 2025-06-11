<?php
// Authentication controller that handles login, register, and password change operations
class AuthController extends BaseController {
    private $user;

    // Initializes the controller with database connection and User model
    public function __construct($db, $service, $request) {
        parent::__construct($db, $service, $request);
        $this->user = new User($db);
    }

    // Processes authentication requests and routes to appropriate methods
    public function processRequest() {
        $action = isset($this->request['path_parts'][1]) ? $this->request['path_parts'][1] : '';

        if ($action === 'login') {
            return $this->login();
        } else if ($action === 'register') {
            return $this->register();
        } else if ($action === 'password') {
            return $this->changePassword();
        } else if ($action === 'me') {
            return $this->getCurrentUser();
        } else {
            return $this->service->notFound('Endpoint not found');
        }
    }

    // Not used for authentication endpoints
    protected function getAll() {
        return $this->service->methodNotAllowed('Method not allowed');
    }

    // Not used for authentication endpoints
    protected function getOne($id) {
        return $this->service->methodNotAllowed('Method not allowed');
    }

    // Routes to register method for user creation
    protected function create() {
        return $this->register();
    }

    // Not used for authentication endpoints
    protected function update($id, $partial = false) {
        return $this->service->methodNotAllowed('Method not allowed');
    }

    // Not used for authentication endpoints
    protected function delete($id) {
        return $this->service->methodNotAllowed('Method not allowed');
    }

    // Not used for authentication endpoints
    protected function getCount() {
        return $this->service->methodNotAllowed('Method not allowed');
    }

    // Authenticates a user with username and password
    public function login() {
        // Get request body
        $data = $this->getBody();

        // Validate request
        if (!isset($data['username']) || !isset($data['password'])) {
            return $this->service->badRequest('Username/email and password are required');
        }

        // Extract credentials
        $username = $data['username'];
        $password = $data['password'];

        // Authenticate user
        $user = $this->user->verify($username, $password);

        if (!$user) {
            return $this->service->unauthorized('Invalid credentials');
        }

        // Simplified for academic project - no JWT tokens
        $token = 'demo-token-' . $user['id'];

        // Return response in format expected by Android app
        return $this->service->success('Login successful', [
            'token' => $token,
            'user' => [
                'id' => $user['id'],
                'username' => $user['username'],
                'email' => $user['email'],
                'user_type' => $user['user_type']
            ]
        ]);
    }

    // Registers a new user account
    public function register() {
        // Get request body
        $data = $this->getBody();

        // Validate request
        if (!isset($data['username']) || !isset($data['email']) || !isset($data['password'])) {
            return $this->service->badRequest('Username, email, and password are required');
        }

        // Check if passwords match
        if (isset($data['confirm_password']) && $data['password'] !== $data['confirm_password']) {
            return $this->service->badRequest('Passwords do not match');
        }

        // Check if user already exists
        $existingUsername = $this->user->getByUsername($data['username']);
        $existingEmail = $this->user->getByEmail($data['email']);

        if ($existingUsername || $existingEmail) {
            return $this->service->conflict('Username or email already exists');
        }

        // Map user_type from Android app format to database format
        $user_type = 'user'; // Default
        if (isset($data['user_type'])) {
            switch (strtoupper($data['user_type'])) {
                case 'LANDLORD':
                    $user_type = 'LANDLORD';
                    break;
                case 'ADMIN':
                    $user_type = 'ADMIN';
                    break;
                default:
                    $user_type = 'user';
            }
        }

        // Create user
        $result = $this->user->create([
            'username' => $data['username'],
            'email' => $data['email'],
            'password' => $data['password'],
            'user_type' => $user_type,
            'full_name' => $data['full_name'] ?? null,
            'phone' => $data['phone'] ?? null
        ]);

        if ($result === -1) {
            return $this->service->conflict('Username or email already exists');
        }

        if (!$result) {
            return $this->service->serverError('Failed to create user');
        }

        // Note: In simplified schema, landlords are stored directly in users table
        // No separate landlord record needed

        // Get the created user
        $user = $this->user->getById($result);

        // Simplified for academic project - no JWT tokens
        $token = 'demo-token-' . $user['id'];

        // Return response in format expected by Android app
        return $this->service->created([
            'token' => $token,
            'user' => [
                'id' => $user['id'],
                'username' => $user['username'],
                'email' => $user['email'],
                'user_type' => $user['user_type']
            ]
        ], 'User registered successfully');
    }

    // Changes the current user's password
    public function changePassword() {
        // Get request body
        $data = $this->getBody();

        // Validate request
        if (!isset($data['current_password']) || !isset($data['new_password'])) {
            return $this->service->badRequest('Current password and new password are required');
        }

        // Get the authenticated user from the request
        if (!isset($this->request['user'])) {
            return $this->service->unauthorized('Authentication required to change password');
        }

        $user = $this->request['user'];
        $userId = $user['id'];

        // Get the current user data from database to verify current password
        $currentUser = $this->user->getByIdWithPassword($userId);
        if (!$currentUser) {
            return $this->service->notFound('User not found');
        }

        // Verify the current password
        if (!password_verify($data['current_password'], $currentUser['password'])) {
            return $this->service->badRequest('Current password is incorrect');
        }

        // Validate new password (basic validation)
        if (strlen($data['new_password']) < 6) {
            return $this->service->badRequest('New password must be at least 6 characters long');
        }

        // Update the password in the database
        $updateData = [
            'password' => $data['new_password'] // User::update() will hash this automatically
        ];

        $result = $this->user->update($userId, $updateData);

        if ($result) {
            error_log("[" . date('c') . "] [INFO] [AuthController] Password changed successfully for user ID: $userId");
            return $this->service->success('Password changed successfully');
        } else {
            error_log("[" . date('c') . "] [ERROR] [AuthController] Failed to update password for user ID: $userId");
            return $this->service->error('Failed to update password. Please try again.');
        }
    }

    // Returns the current authenticated user information
    public function getCurrentUser() {
        // Check if user is authenticated
        if (!isset($this->request['user'])) {
            // Return a guest user if not authenticated
            return $this->service->success('Guest user', [
                'user' => [
                    'id' => 0,
                    'username' => 'guest',
                    'email' => 'guest@rentease.com',
                    'user_type' => 'GUEST'
                ]
            ]);
        }

        $user = $this->request['user'];

        return $this->service->success('User authenticated', [
            'user' => [
                'id' => $user['id'],
                'username' => $user['username'],
                'email' => $user['email'],
                'user_type' => $user['user_type']
            ]
        ]);
    }
}
