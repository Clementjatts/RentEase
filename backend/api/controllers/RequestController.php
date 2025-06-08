<?php
/**
 * Request Controller
 * Handles contact request/notification operations
 */
class RequestController extends BaseController {
    private $requestModel;

    public function __construct($db, $service, $request) {
        parent::__construct($db, $service, $request);
        $this->requestModel = new Request($db);
    }

    /**
     * Override processRequest to handle custom routing
     */
    public function processRequest() {
        $method = $this->request['method'];
        $action = $this->request['action'] ?? '';
        $routeParams = $this->request['route_params'] ?? [];

        try {
            // Handle custom request routes
            if ($method === 'GET' && $action === 'getByLandlord') {
                $landlordId = $routeParams['landlordId'] ?? null;
                return $this->getByLandlord($landlordId);
            }

            if ($method === 'PATCH' && $action === 'markAsRead') {
                $id = $routeParams['id'] ?? null;
                return $this->markAsRead($id);
            }

            if ($method === 'GET' && $action === 'getUnreadCount') {
                $landlordId = $routeParams['landlordId'] ?? null;
                return $this->getUnreadCount($landlordId);
            }

            // For standard CRUD operations, use parent routing
            return parent::processRequest();

        } catch (Exception $e) {
            return $this->service->serverError('Error processing request: ' . $e->getMessage());
        }
    }

    /**
     * Create new request (implements BaseController abstract method)
     */
    protected function create() {
        $data = $this->getBody();

        // Validate required fields
        $required = ['property_id', 'landlord_id', 'requester_name', 'requester_email', 'message'];
        $validation_errors = $this->validateRequired($data, $required);

        if ($validation_errors) {
            return $this->service->badRequest('Validation failed', [
                'errors' => $validation_errors
            ]);
        }

        // Validate email format
        if (!filter_var($data['requester_email'], FILTER_VALIDATE_EMAIL)) {
            return $this->service->badRequest('Invalid email format');
        }

        $requestId = $this->requestModel->create($data);

        if ($requestId) {
            return $this->service->created(['id' => $requestId], 'Request created successfully');
        } else {
            return $this->service->serverError('Failed to create request');
        }
    }

    /**
     * Get requests for landlord
     */
    public function getByLandlord($landlordId) {
        if (!is_numeric($landlordId)) {
            return $this->service->badRequest('Invalid landlord ID');
        }

        $requests = $this->requestModel->getByLandlordId($landlordId);
        return $this->service->success('Requests retrieved successfully', $requests);
    }

    /**
     * Mark request as read
     */
    public function markAsRead($id) {
        if (!is_numeric($id)) {
            return $this->service->badRequest('Invalid request ID');
        }

        $result = $this->requestModel->markAsRead($id);

        if ($result) {
            return $this->service->success('Request marked as read');
        } else {
            return $this->service->serverError('Failed to update request');
        }
    }

    /**
     * Get unread count for landlord
     */
    public function getUnreadCount($landlordId) {
        if (!is_numeric($landlordId)) {
            return $this->service->badRequest('Invalid landlord ID');
        }

        $count = $this->requestModel->getUnreadCount($landlordId);
        return $this->service->success('Unread count retrieved successfully', ['count' => $count]);
    }

    // Required abstract method implementations
    protected function getAll() {
        return $this->service->methodNotAllowed('Method not supported');
    }

    protected function getOne($id) {
        return $this->service->methodNotAllowed('Method not supported');
    }

    protected function update($id, $partial = false) {
        return $this->service->methodNotAllowed('Method not supported');
    }

    protected function delete($id) {
        return $this->service->methodNotAllowed('Method not supported');
    }

    protected function getCount() {
        return $this->service->methodNotAllowed('Method not supported');
    }
}
