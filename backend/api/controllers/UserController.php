<?php
// User controller that handles user-related API requests including landlord management
class UserController extends BaseController {
    private $user;

    // Initializes the controller with database connection and User model
    public function __construct($db, $service, $request) {
        parent::__construct($db, $service, $request);
        $this->user = new User($db);
    }

    // Processes requests and routes to appropriate methods including landlord-specific routes
    public function processRequest() {
        $action = $this->request['action'] ?? '';

        // Handle landlord-specific routes
        if ($action === 'landlords') {
            return $this->getLandlords();
        } elseif ($action === 'landlord-by-user') {
            return $this->getLandlordByUserId();
        } elseif ($action === 'delete-landlord') {
            $routeParams = $this->request['route_params'] ?? [];
            $landlordId = $routeParams['id'] ?? null;
            return $this->deleteLandlord($landlordId);
        } elseif ($action === 'landlord-properties') {
            $routeParams = $this->request['route_params'] ?? [];
            $landlordId = $routeParams['id'] ?? null;
            return $this->getLandlordProperties($landlordId);
        }

        // Use parent routing for standard CRUD operations
        return parent::processRequest();
    }

    // Retrieves all users with admin-only access and pagination
    protected function getAll() {
        try {
            // Only admins can view all users
            if (!$this->isAdmin()) {
                return $this->service->forbidden('Permission denied');
            }

            // Get pagination and query parameters
            $params = array_merge($this->getPaginationParams(), $this->getQueryParams());

            // Get users (supports user_type filtering via query parameter)
            $users = $this->user->getAll($params);

            // Get total count for pagination (respect user_type filtering)
            $total = 0;
            if (!empty($users) || isset($params['user_type'])) {
                $total_query = "SELECT COUNT(*) as total FROM users";
                $total_params = [];

                if (isset($params['user_type'])) {
                    $total_query .= " WHERE user_type = ?";
                    $total_params[] = $params['user_type'];
                }

                $stmt = $this->db->prepare($total_query);
                $stmt->execute($total_params);
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

    // Retrieves a single user by ID with authorization checks
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

    // Creates a new user with validation and admin-only user type setting
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

    // Updates a user with authorization checks and admin-only user type changes
    protected function update($id, $partial = false) {
        try {
            // Users can only update their own profile, unless they're admin
            $user_id = $this->getUserId();
            $is_admin = $this->isAdmin();

            if (!$is_admin && $user_id != $id) {
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

    // Deletes a user with authorization checks
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

    // Returns the total count of users with admin-only access
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

    // Handles user login with credential verification and JWT token generation
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

    // Generates a simplified JWT token for academic project demonstration
    private function generateJWT($data) {
        // Simplified for academic project - return demo token
        return 'demo-token-' . ($data['id'] ?? 'unknown') . '-' . time();
    }

    // Retrieves all landlords with admin-only access and pagination
    public function getLandlords() {
        try {
            // Only admins can view all landlords
            if (!$this->isAdmin()) {
                return $this->service->forbidden('Permission denied: Only admins can view all landlords');
            }

            // Get pagination and query parameters
            $params = array_merge($this->getPaginationParams(), $this->getQueryParams());

            // Filter for landlords only
            $params['user_type'] = 'LANDLORD';

            // Get landlords from users table
            $users = $this->user->getAll($params);

            // Get total count for pagination
            $query = "SELECT COUNT(*) as count FROM users WHERE user_type = 'LANDLORD'";
            $stmt = $this->db->prepare($query);
            $stmt->execute();
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            $total = $result['count'];

            // Build response with consistent field names (no transformation)
            $data = [
                'landlords' => $users, // Use original user data with consistent field names
                'pagination' => [
                    'total' => $total,
                    'page' => $params['page'],
                    'limit' => $params['limit'],
                    'pages' => ceil($total / $params['limit'])
                ]
            ];

            return $this->service->success('Landlords retrieved successfully', $data);
        } catch (Exception $e) {
            return $this->service->serverError('Error retrieving landlords: ' . $e->getMessage());
        }
    }

    // Retrieves landlord profile for the current authenticated user
    public function getLandlordByUserId() {
        try {
            // Get the authenticated user ID
            $user_id = $this->getUserId();

            if (!$user_id) {
                return $this->service->unauthorized('Authentication required');
            }

            // Get user and check if they are a landlord
            $user = $this->user->getById($user_id);

            if (!$user || $user['user_type'] !== 'LANDLORD') {
                return $this->service->notFound('Landlord profile not found for current user');
            }

            // Return user data with consistent field names (no transformation)
            return $this->service->success('Landlord retrieved successfully', $user);
        } catch (Exception $e) {
            return $this->service->serverError('Error retrieving landlord: ' . $e->getMessage());
        }
    }

    // Deletes a landlord with admin-only access and property association checks
    public function deleteLandlord($id) {
        try {
            // Only admins can delete landlords
            if (!$this->isAdmin()) {
                return $this->service->forbidden('Permission denied');
            }

            // Check if landlord exists
            $user = $this->user->getById($id);

            if (!$user || $user['user_type'] !== 'LANDLORD') {
                return $this->service->notFound('Landlord not found');
            }

            // Check if landlord has associated properties
            require_once __DIR__ . '/../models/Property.php';
            $property = new Property($this->db);
            $properties = $property->getAll(['landlord_id' => $id]);

            if (!empty($properties)) {
                return $this->service->conflict('Cannot delete landlord with associated properties');
            }

            // Delete the user (landlord)
            $result = $this->user->delete($id);

            if (!$result) {
                return $this->service->serverError('Failed to delete landlord');
            }

            return $this->service->success('Landlord deleted successfully');
        } catch (Exception $e) {
            return $this->service->serverError('Error deleting landlord: ' . $e->getMessage());
        }
    }

    // Retrieves all properties owned by a specific landlord with pagination
    public function getLandlordProperties($landlord_id) {
        try {
            if (!$landlord_id) {
                return $this->service->badRequest('Landlord ID is required');
            }

            // Check if landlord exists
            $user = $this->user->getById($landlord_id);

            if (!$user || $user['user_type'] !== 'LANDLORD') {
                return $this->service->notFound('Landlord not found');
            }

            // Get pagination parameters
            $params = $this->getPaginationParams();

            // Load property model to get properties
            require_once __DIR__ . '/../models/Property.php';
            $property = new Property($this->db);
            $params['landlord_id'] = $landlord_id;

            // Get properties
            $properties = $property->getAll($params);

            // Get total count for pagination
            $total = $property->getCount(['landlord_id' => $landlord_id]);

            // Build response with consistent field names
            $data = [
                'landlord' => $user, // Use original user data
                'properties' => $properties,
                'pagination' => [
                    'total' => $total,
                    'page' => $params['page'],
                    'limit' => $params['limit'],
                    'pages' => ceil($total / $params['limit'])
                ]
            ];

            return $this->service->success('Landlord properties retrieved successfully', $data);
        } catch (Exception $e) {
            return $this->service->serverError('Error retrieving landlord properties: ' . $e->getMessage());
        }
    }
}
