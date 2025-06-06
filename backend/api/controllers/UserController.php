<?php
/**
 * User Controller
 *
 * Handles user-related API requests
 */
class UserController extends Controller {
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
     * Get all users
     *
     * @return array
     */
    protected function getAll() {
        try {
            // Only admins can view all users
            if (!$this->isAdmin()) {
                return $this->service->forbidden('Permission denied');
            }

            // Get pagination and query parameters
            $params = array_merge($this->getPaginationParams(), $this->getQueryParams());

            // Get users
            $users = $this->user->getAll($params);

            // Get total count for pagination
            $total = 0;
            if (!empty($users)) {
                $total_query = "SELECT COUNT(*) as total FROM users";
                $stmt = $this->db->prepare($total_query);
                $stmt->execute();
                $total = $stmt->fetch(PDO::FETCH_ASSOC)['total'];
            }

            // Build response
            $data = [
                'users' => $users,
                'pagination' => [
                    'total' => $total,
                    'page' => $params['page'],
                    'limit' => $params['limit'],
                    'pages' => ceil($total / $params['limit'])
                ]
            ];

            return $this->service->success('Users retrieved successfully', $data);
        } catch (Exception $e) {
            return $this->service->serverError('Error retrieving users: ' . $e->getMessage());
        }
    }

    /**
     * Get one user by ID
     *
     * @param int $id User ID
     * @return array
     */
    protected function getOne($id) {
        try {
            // Users can only view their own profile, unless they're admin
            $user_id = $this->getUserId();
            if (!$this->isAdmin() && $user_id != $id) {
                return $this->service->forbidden('Permission denied');
            }

            $user = $this->user->getById($id);

            if (!$user) {
                return $this->service->notFound('User not found');
            }

            return $this->service->success('User retrieved successfully', $user);
        } catch (Exception $e) {

            return $this->service->serverError('Error retrieving user: ' . $e->getMessage());
        }
    }

    /**
     * Create a new user
     *
     * @return array
     */
    protected function create() {
        try {
            $data = $this->getBody();

            // Validate required fields
            $required = ['username', 'email', 'password'];
            $validation_errors = $this->validateRequired($data, $required);

            if ($validation_errors) {
                return $this->service->badRequest('Validation failed', [
                    'errors' => $validation_errors
                ]);
            }

            // Only admins can set user types
            if (isset($data['user_type']) && !$this->isAdmin()) {
                unset($data['user_type']);
            }

            // Create the user
            $id = $this->user->create($data);

            if ($id === -1) {
                return $this->service->conflict('Username or email already exists');
            }

            if (!$id) {
                return $this->service->serverError('Failed to create user');
            }

            // Get the created user
            $user = $this->user->getById($id);

            return $this->service->created('User created successfully', $user);
        } catch (Exception $e) {

            return $this->service->serverError('Error creating user: ' . $e->getMessage());
        }
    }

    /**
     * Update a user
     *
     * @param int $id User ID
     * @param bool $partial Whether this is a partial update (PATCH)
     * @return array
     */
    protected function update($id, $partial = false) {
        try {
            // Users can only update their own profile, unless they're admin
            $user_id = $this->getUserId();
            if (!$this->isAdmin() && $user_id != $id) {
                return $this->service->forbidden('Permission denied');
            }

            // Check if user exists
            $user = $this->user->getById($id);

            if (!$user) {
                return $this->service->notFound('User not found');
            }

            $data = $this->getBody();

            // For PUT requests (complete update), validate required fields
            if (!$partial) {
                $required = ['username', 'email'];
                $validation_errors = $this->validateRequired($data, $required);

                if ($validation_errors) {
                    return $this->service->badRequest('Validation failed', [
                        'errors' => $validation_errors
                    ]);
                }
            }

            // Only admins can update user types
            if (isset($data['user_type']) && !$this->isAdmin()) {
                unset($data['user_type']);
            }

            // Update the user
            $result = $this->user->update($id, $data);

            if (!$result) {
                return $this->service->conflict('Username or email already exists, or update failed');
            }

            // Get the updated user
            $updated_user = $this->user->getById($id);

            return $this->service->success('User updated successfully', $updated_user);
        } catch (Exception $e) {

            return $this->service->serverError('Error updating user: ' . $e->getMessage());
        }
    }

    /**
     * Delete a user
     *
     * @param int $id User ID
     * @return array
     */
    protected function delete($id) {
        try {
            // Only admins can delete users, or users can delete their own account
            $user_id = $this->getUserId();
            if (!$this->isAdmin() && $user_id != $id) {
                return $this->service->forbidden('Permission denied');
            }

            // Check if user exists
            $user = $this->user->getById($id);

            if (!$user) {
                return $this->service->notFound('User not found');
            }

            // Delete the user
            $result = $this->user->delete($id);

            if (!$result) {
                return $this->service->conflict('Cannot delete user with associated data');
            }

            return $this->service->success('User deleted successfully');
        } catch (Exception $e) {

            return $this->service->serverError('Error deleting user: ' . $e->getMessage());
        }
    }

    /**
     * Get count of users
     *
     * @return array
     */
    protected function getCount() {
        try {
            // Only admins can view user count
            if (!$this->isAdmin()) {
                return $this->service->forbidden('Permission denied');
            }

            $total_query = "SELECT COUNT(*) as total FROM users";
            $stmt = $this->db->prepare($total_query);
            $stmt->execute();
            $total = $stmt->fetch(PDO::FETCH_ASSOC)['total'];

            return $this->service->success('User count retrieved successfully', [
                'count' => $total
            ]);
        } catch (Exception $e) {

            return $this->service->serverError('Error retrieving user count: ' . $e->getMessage());
        }
    }

    /**
     * Login route (custom route)
     *
     * @return array
     */
    public function login() {
        try {
            $data = $this->getBody();

            // Validate required fields
            $required = ['username', 'password'];
            $validation_errors = $this->validateRequired($data, $required);

            if ($validation_errors) {
                return $this->service->badRequest('Validation failed', [
                    'errors' => $validation_errors
                ]);
            }

            // Verify credentials
            $user = $this->user->verify($data['username'], $data['password']);

            if (!$user) {
                return $this->service->unauthorized('Invalid credentials');
            }

            // Generate JWT token
            $jwt_data = [
                'id' => $user['id'],
                'username' => $user['username'],
                'email' => $user['email'],
                'user_type' => $user['user_type']
            ];

            $jwt = $this->generateJWT($jwt_data);

            return $this->service->success('Login successful', [
                'user' => $user,
                'token' => $jwt
            ]);
        } catch (Exception $e) {

            return $this->service->serverError('Error during login: ' . $e->getMessage());
        }
    }

    /**
     * Generate JWT token (simplified for academic project)
     *
     * @param array $data Data to encode in the token
     * @return string Demo token
     */
    private function generateJWT($data) {
        // Simplified for academic project - return demo token
        return 'demo-token-' . ($data['id'] ?? 'unknown') . '-' . time();
    }
}
