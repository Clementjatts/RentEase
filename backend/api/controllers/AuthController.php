<?php
/**
 * Auth Controller
 *
 * Handles authentication-related API requests
 */

class AuthController extends Controller {
    private $user;

    /**
     * Constructor
     *
     * @param PDO $db Database connection
     * @param ResponseService $service Response service
     * @param array $request Request data
     */
    public function __construct($db, $service, $request) {
        parent::__construct($db, $service, $request);
        $this->user = new User($db);
    }

    /**
     * Process the request and route to the appropriate method
     *
     * @return array Response data
     */
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

    /**
     * Get all resources - Not used for Auth
     *
     * @return array
     */
    protected function getAll() {
        return $this->service->methodNotAllowed('Method not allowed');
    }

    /**
     * Get one resource by ID - Not used for Auth
     *
     * @param int $id Resource ID
     * @return array
     */
    protected function getOne($id) {
        return $this->service->methodNotAllowed('Method not allowed');
    }

    /**
     * Create a new resource - Used for register
     *
     * @return array
     */
    protected function create() {
        return $this->register();
    }

    /**
     * Update a resource - Not used for Auth
     *
     * @param int $id Resource ID
     * @param bool $partial Whether this is a partial update (PATCH)
     * @return array
     */
    protected function update($id, $partial = false) {
        return $this->service->methodNotAllowed('Method not allowed');
    }

    /**
     * Delete a resource - Not used for Auth
     *
     * @param int $id Resource ID
     * @return array
     */
    protected function delete($id) {
        return $this->service->methodNotAllowed('Method not allowed');
    }

    /**
     * Get count of resources - Not used for Auth
     *
     * @return array
     */
    protected function getCount() {
        return $this->service->methodNotAllowed('Method not allowed');
    }

    /**
     * Login a user
     *
     * @return array
     */
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

    /**
     * Register a new user
     *
     * @return array
     */
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

        // If user is a LANDLORD, create corresponding landlord record
        if ($user_type === 'LANDLORD' && isset($data['full_name']) && isset($data['phone'])) {
            $landlord = new Landlord($this->db);
            $landlord_id = $landlord->create([
                'name' => $data['full_name'],
                'contact' => $data['phone'],
                'email' => $data['email'],
                'user_id' => $result  // Link to the user record
            ]);

            if (!$landlord_id) {
                // Log error but don't fail registration - user can still be created manually later
                error_log("Failed to create landlord record for user ID: $result");
            }
        }

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

    /**
     * Change user password
     *
     * @return array
     */
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

    /**
     * Get the current authenticated user
     *
     * @return array
     */
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
