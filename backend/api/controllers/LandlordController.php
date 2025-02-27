<?php
/**
 * Landlord Controller
 * 
 * Handles landlord-related API requests
 */
class LandlordController extends Controller {
    private $landlord;
    
    /**
     * Constructor
     * 
     * @param PDO $db Database connection
     * @param ResponseService $service Response service
     * @param array $request Request data
     */
    public function __construct($db, $service, $request) {
        parent::__construct($db, $service, $request);
        $this->landlord = new Landlord($db);
    }
    
    /**
     * Get all landlords
     * 
     * @return array
     */
    protected function getAll() {
        try {
            // Get pagination and query parameters
            $params = array_merge($this->getPaginationParams(), $this->getQueryParams());
            
            // Get landlords
            $landlords = $this->landlord->getAll($params);
            
            // Get total count for pagination
            $total = $this->landlord->getCount($params);
            
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
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving landlords: ' . $e->getMessage());
        }
    }
    
    /**
     * Get one landlord by ID
     * 
     * @param int $id Landlord ID
     * @return array
     */
    protected function getOne($id) {
        try {
            $landlord = $this->landlord->getById($id);
            
            if (!$landlord) {
                return $this->service->notFound('Landlord not found');
            }
            
            return $this->service->success('Landlord retrieved successfully', $landlord);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving landlord: ' . $e->getMessage());
        }
    }
    
    /**
     * Create a new landlord
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
            
            // Validate required fields
            $required = ['name', 'contact'];
            $validation_errors = $this->validateRequired($data, $required);
            
            if ($validation_errors) {
                return $this->service->badRequest('Validation failed', [
                    'errors' => $validation_errors
                ]);
            }
            
            // Create the landlord
            $id = $this->landlord->create($data);
            
            if ($id === -1) {
                return $this->service->conflict('Email already exists');
            }
            
            if (!$id) {
                return $this->service->serverError('Failed to create landlord');
            }
            
            // Get the created landlord
            $landlord = $this->landlord->getById($id);
            
            return $this->service->created('Landlord created successfully', $landlord);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error creating landlord: ' . $e->getMessage());
        }
    }
    
    /**
     * Update a landlord
     * 
     * @param int $id Landlord ID
     * @param bool $partial Whether this is a partial update (PATCH)
     * @return array
     */
    protected function update($id, $partial = false) {
        try {
            // Only admins can update landlords
            if (!$this->isAdmin()) {
                return $this->service->forbidden('Permission denied');
            }
            
            // Check if landlord exists
            $landlord = $this->landlord->getById($id);
            
            if (!$landlord) {
                return $this->service->notFound('Landlord not found');
            }
            
            $data = $this->getBody();
            
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
            
            // Update the landlord
            $result = $this->landlord->update($id, $data);
            
            if (!$result) {
                return $this->service->conflict('Email already exists or update failed');
            }
            
            // Get the updated landlord
            $updated_landlord = $this->landlord->getById($id);
            
            return $this->service->success('Landlord updated successfully', $updated_landlord);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error updating landlord: ' . $e->getMessage());
        }
    }
    
    /**
     * Delete a landlord
     * 
     * @param int $id Landlord ID
     * @return array
     */
    protected function delete($id) {
        try {
            // Only admins can delete landlords
            if (!$this->isAdmin()) {
                return $this->service->forbidden('Permission denied');
            }
            
            // Check if landlord exists
            $landlord = $this->landlord->getById($id);
            
            if (!$landlord) {
                return $this->service->notFound('Landlord not found');
            }
            
            // Delete the landlord
            $result = $this->landlord->delete($id);
            
            if (!$result) {
                return $this->service->conflict('Cannot delete landlord with associated properties');
            }
            
            return $this->service->success('Landlord deleted successfully');
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error deleting landlord: ' . $e->getMessage());
        }
    }
    
    /**
     * Get count of landlords
     * 
     * @return array
     */
    protected function getCount() {
        try {
            $params = $this->getQueryParams();
            $count = $this->landlord->getCount($params);
            
            return $this->service->success('Landlord count retrieved successfully', [
                'count' => $count
            ]);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
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
            $landlord = $this->landlord->getById($landlord_id);
            
            if (!$landlord) {
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
                    'id' => $landlord['id'],
                    'name' => $landlord['name'],
                    'contact' => $landlord['contact'],
                    'email' => $landlord['email']
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
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving landlord properties: ' . $e->getMessage());
        }
    }
    
    /**
     * Get requests by landlord
     * 
     * @return array
     */
    public function requests() {
        try {
            // Only admins and landlords can view landlord requests
            $user_role = $this->getUserRole();
            if ($user_role !== 'admin' && $user_role !== 'landlord') {
                return $this->service->forbidden('Permission denied');
            }
            
            // Get landlord ID from request path
            $landlord_id = isset($this->request['path_parts'][1]) ? (int)$this->request['path_parts'][1] : null;
            
            if (!$landlord_id) {
                return $this->service->badRequest('Landlord ID is required');
            }
            
            // Check if landlord exists
            $landlord = $this->landlord->getById($landlord_id);
            
            if (!$landlord) {
                return $this->service->notFound('Landlord not found');
            }
            
            // Get pagination parameters
            $params = $this->getPaginationParams();
            
            // Load request model to get requests
            $request_model = new Request($this->db);
            $params['landlord_id'] = $landlord_id;
            
            // Get requests
            $requests = $request_model->getAll($params);
            
            // Get total count for pagination
            $total = $request_model->getCount(['landlord_id' => $landlord_id]);
            
            // Build response
            $data = [
                'landlord' => [
                    'id' => $landlord['id'],
                    'name' => $landlord['name'],
                    'contact' => $landlord['contact'],
                    'email' => $landlord['email']
                ],
                'requests' => $requests,
                'pagination' => [
                    'total' => $total,
                    'page' => $params['page'],
                    'limit' => $params['limit'],
                    'pages' => ceil($total / $params['limit'])
                ]
            ];
            
            return $this->service->success('Landlord requests retrieved successfully', $data);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving landlord requests: ' . $e->getMessage());
        }
    }
}
