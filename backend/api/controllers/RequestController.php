<?php
/**
 * Request Controller
 * 
 * Handles property viewing request-related API endpoints
 */
class RequestController extends Controller {
    private $request_model;
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
        $this->request_model = new Request($db);
        $this->property = new Property($db);
    }
    
    /**
     * Get all requests
     * 
     * @return array
     */
    protected function getAll() {
        try {
            // Get pagination and query parameters
            $params = array_merge($this->getPaginationParams(), $this->getQueryParams());
            
            // Users can only view their own requests, unless they're admin or landlord
            $user_role = $this->getUserRole();
            if ($user_role !== 'admin' && $user_role !== 'landlord') {
                $params['user_id'] = $this->getUserId();
            }
            
            // Get requests
            $requests = $this->request_model->getAll($params);
            
            // Get total count for pagination
            $total = $this->request_model->getCount($params);
            
            // Build response
            $data = [
                'requests' => $requests,
                'pagination' => [
                    'total' => $total,
                    'page' => $params['page'],
                    'limit' => $params['limit'],
                    'pages' => ceil($total / $params['limit'])
                ]
            ];
            
            return $this->service->success('Requests retrieved successfully', $data);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving requests: ' . $e->getMessage());
        }
    }
    
    /**
     * Get one request by ID
     * 
     * @param int $id Request ID
     * @return array
     */
    protected function getOne($id) {
        try {
            $request = $this->request_model->getById($id);
            
            if (!$request) {
                return $this->service->notFound('Request not found');
            }
            
            // Users can only view their own requests, unless they're admin or landlord
            $user_role = $this->getUserRole();
            if ($user_role !== 'admin' && $user_role !== 'landlord' && $request['user_id'] != $this->getUserId()) {
                return $this->service->forbidden('Permission denied');
            }
            
            return $this->service->success('Request retrieved successfully', $request);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving request: ' . $e->getMessage());
        }
    }
    
    /**
     * Create a new request
     * 
     * @return array
     */
    protected function create() {
        try {
            $data = $this->getBody();
            
            // Validate required fields
            $required = ['property_id', 'requested_date'];
            $validation_errors = $this->validateRequired($data, $required);
            
            if ($validation_errors) {
                return $this->service->badRequest('Validation failed', [
                    'errors' => $validation_errors
                ]);
            }
            
            // Set user_id from authenticated user
            $data['user_id'] = $this->getUserId();
            
            // Validate that the property exists and is available
            $property = $this->property->getById($data['property_id']);
            if (!$property) {
                return $this->service->notFound('Property not found');
            }
            
            if ($property['status'] !== 'available') {
                return $this->service->conflict('Property is not available for viewing');
            }
            
            // Create the request
            $id = $this->request_model->create($data);
            
            if ($id === -1) {
                return $this->service->conflict('You already have a pending or approved request for this property');
            }
            
            if (!$id) {
                return $this->service->serverError('Failed to create request');
            }
            
            // Get the created request
            $request = $this->request_model->getById($id);
            
            return $this->service->created('Request submitted successfully', $request);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error creating request: ' . $e->getMessage());
        }
    }
    
    /**
     * Update a request
     * 
     * @param int $id Request ID
     * @param bool $partial Whether this is a partial update (PATCH)
     * @return array
     */
    protected function update($id, $partial = false) {
        try {
            // Check if request exists
            $request = $this->request_model->getById($id);
            
            if (!$request) {
                return $this->service->notFound('Request not found');
            }
            
            $data = $this->getBody();
            $user_role = $this->getUserRole();
            
            // Regular users can only update their own requests and only certain fields
            if ($user_role === 'user') {
                if ($request['user_id'] != $this->getUserId()) {
                    return $this->service->forbidden('Permission denied');
                }
                
                // Users can only update the date and message, not the status
                if (isset($data['status'])) {
                    unset($data['status']);
                }
                
                // Users can't update requests that are already approved or canceled
                if ($request['status'] === 'approved' || $request['status'] === 'canceled') {
                    return $this->service->conflict('Cannot update approved or canceled requests');
                }
            }
            
            // For PUT requests (complete update), validate required fields
            if (!$partial) {
                $required = ['requested_date'];
                $validation_errors = $this->validateRequired($data, $required);
                
                if ($validation_errors) {
                    return $this->service->badRequest('Validation failed', [
                        'errors' => $validation_errors
                    ]);
                }
            }
            
            // Update the request
            $result = $this->request_model->update($id, $data);
            
            if (!$result) {
                return $this->service->serverError('Failed to update request');
            }
            
            // Get the updated request
            $updated_request = $this->request_model->getById($id);
            
            return $this->service->success('Request updated successfully', $updated_request);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error updating request: ' . $e->getMessage());
        }
    }
    
    /**
     * Delete a request
     * 
     * @param int $id Request ID
     * @return array
     */
    protected function delete($id) {
        try {
            // Check if request exists
            $request = $this->request_model->getById($id);
            
            if (!$request) {
                return $this->service->notFound('Request not found');
            }
            
            // Users can only delete their own requests, unless they're admin
            if (!$this->isAdmin() && $request['user_id'] != $this->getUserId()) {
                return $this->service->forbidden('Permission denied');
            }
            
            // Delete the request
            $result = $this->request_model->delete($id);
            
            if (!$result) {
                return $this->service->serverError('Failed to delete request');
            }
            
            return $this->service->success('Request deleted successfully');
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error deleting request: ' . $e->getMessage());
        }
    }
    
    /**
     * Get count of requests
     * 
     * @return array
     */
    protected function getCount() {
        try {
            $params = $this->getQueryParams();
            
            // Users can only count their own requests, unless they're admin or landlord
            $user_role = $this->getUserRole();
            if ($user_role !== 'admin' && $user_role !== 'landlord') {
                $params['user_id'] = $this->getUserId();
            }
            
            $count = $this->request_model->getCount($params);
            
            return $this->service->success('Request count retrieved successfully', [
                'count' => $count
            ]);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving request count: ' . $e->getMessage());
        }
    }
    
    /**
     * Approve a request
     * 
     * @return array
     */
    public function approve() {
        try {
            // Only admins and landlords can approve requests
            $user_role = $this->getUserRole();
            if ($user_role !== 'admin' && $user_role !== 'landlord') {
                return $this->service->forbidden('Permission denied');
            }
            
            $data = $this->getBody();
            
            // Validate request ID
            if (!isset($data['id']) || empty($data['id'])) {
                return $this->service->badRequest('Request ID is required');
            }
            
            $id = (int)$data['id'];
            
            // Check if request exists
            $request = $this->request_model->getById($id);
            
            if (!$request) {
                return $this->service->notFound('Request not found');
            }
            
            // Update the request status
            $update_data = [
                'status' => 'approved'
            ];
            
            $result = $this->request_model->update($id, $update_data);
            
            if (!$result) {
                return $this->service->serverError('Failed to approve request');
            }
            
            // Get the updated request
            $updated_request = $this->request_model->getById($id);
            
            return $this->service->success('Request approved successfully', $updated_request);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error approving request: ' . $e->getMessage());
        }
    }
    
    /**
     * Reject a request
     * 
     * @return array
     */
    public function reject() {
        try {
            // Only admins and landlords can reject requests
            $user_role = $this->getUserRole();
            if ($user_role !== 'admin' && $user_role !== 'landlord') {
                return $this->service->forbidden('Permission denied');
            }
            
            $data = $this->getBody();
            
            // Validate request ID
            if (!isset($data['id']) || empty($data['id'])) {
                return $this->service->badRequest('Request ID is required');
            }
            
            $id = (int)$data['id'];
            
            // Check if request exists
            $request = $this->request_model->getById($id);
            
            if (!$request) {
                return $this->service->notFound('Request not found');
            }
            
            // Update the request status
            $update_data = [
                'status' => 'rejected'
            ];
            
            // Add rejection reason if provided
            if (isset($data['message']) && !empty($data['message'])) {
                $update_data['message'] = $data['message'];
            }
            
            $result = $this->request_model->update($id, $update_data);
            
            if (!$result) {
                return $this->service->serverError('Failed to reject request');
            }
            
            // Get the updated request
            $updated_request = $this->request_model->getById($id);
            
            return $this->service->success('Request rejected successfully', $updated_request);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error rejecting request: ' . $e->getMessage());
        }
    }
    
    /**
     * Cancel a request
     * 
     * @return array
     */
    public function cancel() {
        try {
            $data = $this->getBody();
            
            // Validate request ID
            if (!isset($data['id']) || empty($data['id'])) {
                return $this->service->badRequest('Request ID is required');
            }
            
            $id = (int)$data['id'];
            
            // Check if request exists
            $request = $this->request_model->getById($id);
            
            if (!$request) {
                return $this->service->notFound('Request not found');
            }
            
            // Users can only cancel their own requests
            if (!$this->isAdmin() && $request['user_id'] != $this->getUserId()) {
                return $this->service->forbidden('Permission denied');
            }
            
            // Update the request status
            $update_data = [
                'status' => 'canceled'
            ];
            
            // Add cancellation reason if provided
            if (isset($data['message']) && !empty($data['message'])) {
                $update_data['message'] = $data['message'];
            }
            
            $result = $this->request_model->update($id, $update_data);
            
            if (!$result) {
                return $this->service->serverError('Failed to cancel request');
            }
            
            // Get the updated request
            $updated_request = $this->request_model->getById($id);
            
            return $this->service->success('Request canceled successfully', $updated_request);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error canceling request: ' . $e->getMessage());
        }
    }
}
