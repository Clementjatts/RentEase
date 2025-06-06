<?php
/**
 * Landlord Controller (Simplified Schema)
 *
 * Handles landlord-related API requests using the simplified users table
 * Landlords are users with user_type = 'LANDLORD'
 */
class LandlordController extends Controller {
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
        $action = $this->request['action'] ?? '';

        // Handle special routes
        if ($action === 'getByUserId') {
            return $this->getByUserId();
        } elseif ($action === 'properties') {
            return $this->properties();
        }

        // Use parent routing for standard CRUD operations
        return parent::processRequest();
    }

    /**
     * Get all landlords (from users table where user_type = 'LANDLORD')
     *
     * @return array
     */
    protected function getAll() {
        try {
            // Get pagination and query parameters
            $params = array_merge($this->getPaginationParams(), $this->getQueryParams());

            // Filter for landlords only
            $params['user_type'] = 'LANDLORD';

            // Get landlords from users table
            $users = $this->user->getAll($params);

            // Transform user data to landlord format for API compatibility
            $landlords = array_map(function($user) {
                return [
                    'id' => $user['id'], // user_id is now landlord_id
                    'user_id' => $user['id'],
                    'name' => $user['full_name'],
                    'contact' => $user['phone'],
                    'email' => $user['email'],
                    'created_at' => $user['created_at']
                ];
            }, $users);

            // Get total count for pagination
            $query = "SELECT COUNT(*) as count FROM users WHERE user_type = 'LANDLORD'";
            $stmt = $this->db->prepare($query);
            $stmt->execute();
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            $total = $result['count'];

            // Build response
            $data = [
                'landlords' => $landlords,
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

    /**
     * Get one landlord by ID (from users table)
     *
     * @param int $id User ID (landlord ID)
     * @return array
     */
    protected function getOne($id) {
        try {
            $user = $this->user->getById($id);

            if (!$user || $user['user_type'] !== 'LANDLORD') {
                return $this->service->notFound('Landlord not found');
            }

            // Transform user data to landlord format for API compatibility
            $landlord = [
                'id' => $user['id'],
                'user_id' => $user['id'],
                'name' => $user['full_name'],
                'contact' => $user['phone'],
                'email' => $user['email'],
                'created_at' => $user['created_at']
            ];

            return $this->service->success('Landlord retrieved successfully', $landlord);
        } catch (Exception $e) {
            return $this->service->serverError('Error retrieving landlord: ' . $e->getMessage());
        }
    }

    /**
     * Create a new landlord (create user with user_type = 'LANDLORD')
     *
     * @return array
     */
    protected function create() {
        try {
            // Only admins can create landlords
            if (!$this->isAdmin()) {
                return $this->service->forbidden('Permission denied');
            }

            $data = $this->getBody();

            // Validate required fields (map from landlord format to user format)
            $required = ['name', 'contact', 'email', 'username', 'password'];
            $validation_errors = $this->validateRequired($data, $required);

            if ($validation_errors) {
                return $this->service->badRequest('Validation failed', [
                    'errors' => $validation_errors
                ]);
            }

            // Transform landlord data to user format
            $userData = [
                'username' => $data['username'],
                'password' => $data['password'],
                'email' => $data['email'],
                'phone' => $data['contact'],
                'user_type' => 'LANDLORD',
                'full_name' => $data['name']
            ];

            // Create the user (landlord)
            $id = $this->user->create($userData);

            if ($id === -1) {
                return $this->service->conflict('Email or username already exists');
            }

            if (!$id) {
                return $this->service->serverError('Failed to create landlord');
            }

            // Get the created user and transform to landlord format
            $user = $this->user->getById($id);
            $landlord = [
                'id' => $user['id'],
                'user_id' => $user['id'],
                'name' => $user['full_name'],
                'contact' => $user['phone'],
                'email' => $user['email'],
                'created_at' => $user['created_at']
            ];

            return $this->service->created('Landlord created successfully', $landlord);
        } catch (Exception $e) {
            return $this->service->serverError('Error creating landlord: ' . $e->getMessage());
        }
    }

    /**
     * Update a landlord (update user with user_type = 'LANDLORD')
     *
     * @param int $id User ID (landlord ID)
     * @param bool $partial Whether this is a partial update (PATCH)
     * @return array
     */
    protected function update($id, $partial = false) {
        try {
            // Check if landlord exists first
            $user = $this->user->getById($id);

            if (!$user || $user['user_type'] !== 'LANDLORD') {
                return $this->service->notFound('Landlord not found');
            }

            // Authorization: Admins can update any landlord, landlords can only update their own profile
            $current_user_id = $this->getUserId();
            $is_admin = $this->isAdmin();
            $is_own_profile = ($current_user_id && $user['id'] == $current_user_id);

            if (!$is_admin && !$is_own_profile) {
                return $this->service->forbidden('Permission denied');
            }

            $data = $this->getBody();

            // Transform landlord data to user format
            $userData = [];
            if (isset($data['name'])) $userData['full_name'] = $data['name'];
            if (isset($data['contact'])) $userData['phone'] = $data['contact'];
            if (isset($data['email'])) $userData['email'] = $data['email'];
            if (isset($data['username'])) $userData['username'] = $data['username'];
            if (isset($data['password'])) $userData['password'] = $data['password'];

            // For PUT requests (complete update), validate required fields
            if (!$partial) {
                $required = ['name', 'contact'];
                $validation_errors = $this->validateRequired($data, $required);

                if ($validation_errors) {
                    return $this->service->badRequest('Validation failed', [
                        'errors' => $validation_errors
                    ]);
                }
            }

            // Update the user (landlord)
            $result = $this->user->update($id, $userData);

            if (!$result) {
                return $this->service->conflict('Email or username already exists or update failed');
            }

            // Get the updated user and transform to landlord format
            $updated_user = $this->user->getById($id);
            $updated_landlord = [
                'id' => $updated_user['id'],
                'user_id' => $updated_user['id'],
                'name' => $updated_user['full_name'],
                'contact' => $updated_user['phone'],
                'email' => $updated_user['email'],
                'created_at' => $updated_user['created_at']
            ];

            return $this->service->success('Landlord updated successfully', $updated_landlord);
        } catch (Exception $e) {
            return $this->service->serverError('Error updating landlord: ' . $e->getMessage());
        }
    }

    /**
     * Delete a landlord (delete user with user_type = 'LANDLORD')
     *
     * @param int $id User ID (landlord ID)
     * @return array
     */
    protected function delete($id) {
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

    /**
     * Get count of landlords (count users with user_type = 'LANDLORD')
     *
     * @return array
     */
    protected function getCount() {
        try {
            $params = $this->getQueryParams();
            $params['user_type'] = 'LANDLORD';

            // Simple count query for landlords
            $query = "SELECT COUNT(*) as count FROM users WHERE user_type = 'LANDLORD'";
            $stmt = $this->db->prepare($query);
            $stmt->execute();
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            $count = $result['count'];

            return $this->service->success('Landlord count retrieved successfully', [
                'count' => $count
            ]);
        } catch (Exception $e) {
            return $this->service->serverError('Error retrieving landlord count: ' . $e->getMessage());
        }
    }

    /**
     * Get properties by landlord
     *
     * @return array
     */
    public function properties() {
        try {
            // Get landlord ID from request path
            $landlord_id = isset($this->request['path_parts'][1]) ? (int)$this->request['path_parts'][1] : null;

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
            $property = new Property($this->db);
            $params['landlord_id'] = $landlord_id;

            // Get properties
            $properties = $property->getAll($params);

            // Get total count for pagination
            $total = $property->getCount(['landlord_id' => $landlord_id]);

            // Build response
            $data = [
                'landlord' => [
                    'id' => $user['id'],
                    'name' => $user['full_name'],
                    'contact' => $user['phone'],
                    'email' => $user['email']
                ],
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

    /**
     * Get landlord by user ID (for current authenticated user)
     *
     * @return array
     */
    public function getByUserId() {
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

            // Transform user data to landlord format
            $landlord = [
                'id' => $user['id'],
                'user_id' => $user['id'],
                'name' => $user['full_name'],
                'contact' => $user['phone'],
                'email' => $user['email'],
                'created_at' => $user['created_at']
            ];

            return $this->service->success('Landlord retrieved successfully', $landlord);
        } catch (Exception $e) {
            return $this->service->serverError('Error retrieving landlord: ' . $e->getMessage());
        }
    }
}
