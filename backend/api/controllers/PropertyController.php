<?php
/**
 * Property Controller
 * 
 * Handles property-related API requests
 */
class PropertyController extends Controller {
    private $property;
    
    /**
     * Constructor
     * 
     * @param PDO $db Database connection
     * @param ResponseService $service Response service
     * @param array $request Request data
     */
    public function __construct($db, $service, $request) {
        parent::__construct($db, $service, $request);
        $this->property = new Property($db);
    }
    
    /**
     * Get all properties
     * 
     * @return array
     */
    protected function getAll() {
        try {
            // Get pagination and query parameters
            $params = array_merge($this->getPaginationParams(), $this->getQueryParams());
            
            // Get properties
            $properties = $this->property->getAll($params);
            
            // Get total count for pagination
            $total = $this->property->getCount($params);
            
            // Build response
            $data = [
                'properties' => $properties,
                'pagination' => [
                    'total' => $total,
                    'page' => $params['page'],
                    'limit' => $params['limit'],
                    'pages' => ceil($total / $params['limit'])
                ]
            ];
            
            return $this->service->success('Properties retrieved successfully', $data);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving properties: ' . $e->getMessage());
        }
    }
    
    /**
     * Get one property by ID
     * 
     * @param int $id Property ID
     * @return array
     */
    protected function getOne($id) {
        try {
            $property = $this->property->getById($id);
            
            if (!$property) {
                return $this->service->notFound('Property not found');
            }
            
            return $this->service->success('Property retrieved successfully', $property);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving property: ' . $e->getMessage());
        }
    }
    
    /**
     * Create a new property
     * 
     * @return array
     */
    protected function create() {
        try {
            // Only admins and landlords can create properties
            if (!$this->isAdmin() && $this->getUserRole() !== 'landlord') {
                return $this->service->forbidden('Permission denied');
            }
            
            $data = $this->getBody();
            
            // Validate required fields
            $required = ['title', 'landlord_id', 'price', 'bedroom_count', 'bathroom_count', 'size'];
            $validation_errors = $this->validateRequired($data, $required);
            
            if ($validation_errors) {
                return $this->service->badRequest('Validation failed', [
                    'errors' => $validation_errors
                ]);
            }
            
            // Set default values for optional fields
            $data['status'] = $data['status'] ?? 'available';
            $data['furniture_type'] = $data['furniture_type'] ?? 'unfurnished';
            
            // Create the property
            $id = $this->property->create($data);
            
            if (!$id) {
                return $this->service->serverError('Failed to create property');
            }
            
            // Get the created property
            $property = $this->property->getById($id);
            
            return $this->service->created('Property created successfully', $property);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error creating property: ' . $e->getMessage());
        }
    }
    
    /**
     * Update a property
     * 
     * @param int $id Property ID
     * @param bool $partial Whether this is a partial update (PATCH)
     * @return array
     */
    protected function update($id, $partial = false) {
        try {
            // Only admins and landlords can update properties
            if (!$this->isAdmin() && $this->getUserRole() !== 'landlord') {
                return $this->service->forbidden('Permission denied');
            }
            
            // Check if property exists
            $property = $this->property->getById($id);
            
            if (!$property) {
                return $this->service->notFound('Property not found');
            }
            
            $data = $this->getBody();
            
            // For PUT requests (complete update), validate required fields
            if (!$partial) {
                $required = ['title', 'landlord_id', 'price', 'bedroom_count', 'bathroom_count', 'size'];
                $validation_errors = $this->validateRequired($data, $required);
                
                if ($validation_errors) {
                    return $this->service->badRequest('Validation failed', [
                        'errors' => $validation_errors
                    ]);
                }
            }
            
            // Update the property
            $result = $this->property->update($id, $data);
            
            if (!$result) {
                return $this->service->serverError('Failed to update property');
            }
            
            // Get the updated property
            $updated_property = $this->property->getById($id);
            
            return $this->service->success('Property updated successfully', $updated_property);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error updating property: ' . $e->getMessage());
        }
    }
    
    /**
     * Delete a property
     * 
     * @param int $id Property ID
     * @return array
     */
    protected function delete($id) {
        try {
            // Only admins and landlords can delete properties
            if (!$this->isAdmin() && $this->getUserRole() !== 'landlord') {
                return $this->service->forbidden('Permission denied');
            }
            
            // Check if property exists
            $property = $this->property->getById($id);
            
            if (!$property) {
                return $this->service->notFound('Property not found');
            }
            
            // Delete the property
            $result = $this->property->delete($id);
            
            if (!$result) {
                return $this->service->serverError('Failed to delete property');
            }
            
            return $this->service->success('Property deleted successfully');
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error deleting property: ' . $e->getMessage());
        }
    }
    
    /**
     * Get count of properties
     * 
     * @return array
     */
    protected function getCount() {
        try {
            $params = $this->getQueryParams();
            $count = $this->property->getCount($params);
            
            return $this->service->success('Property count retrieved successfully', [
                'count' => $count
            ]);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving property count: ' . $e->getMessage());
        }
    }
}
