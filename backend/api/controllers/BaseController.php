<?php
// Base controller class providing common functionality for all API controllers
abstract class BaseController {
    protected $db;
    protected $service;
    protected $request;
    protected $response;

    // Initializes the controller with database connection, response service, and request data
    public function __construct($db, $service, $request) {
        $this->db = $db;
        $this->service = $service;
        $this->request = $request;
    }

    // Processes the incoming request and routes to the appropriate method
    public function processRequest() {
        $method = $this->request['method'];
        $action = $this->request['action'] ?? (isset($this->request['path_parts'][1]) ? $this->request['path_parts'][1] : '');

        // Simplified for academic project - no logging

        try {
            // Route to the appropriate method based on HTTP method and action
            switch ($method) {
                case 'GET':
                    if (empty($action)) {
                        return $this->getAll();
                    } elseif ($action === 'count') {
                        return $this->getCount();
                    } elseif (is_numeric($action)) {
                        return $this->getOne($action);
                    } else {
                        return $this->service->notFound('Resource not found');
                    }
                    break;

                case 'POST':
                    return $this->create();
                    break;

                case 'PUT':
                    if (is_numeric($action)) {
                        return $this->update($action);
                    } else {
                        return $this->service->notFound('Resource not found');
                    }
                    break;

                case 'PATCH':
                    if (is_numeric($action)) {
                        return $this->update($action, true);
                    } else {
                        return $this->service->notFound('Resource not found');
                    }
                    break;

                case 'DELETE':
                    if (is_numeric($action)) {
                        return $this->delete($action);
                    } else {
                        return $this->service->notFound('Resource not found');
                    }
                    break;

                default:
                    return $this->service->methodNotAllowed('Method not allowed');
            }
        } catch (Exception $e) {
            return $this->service->serverError('Internal server error: ' . $e->getMessage());
        }
    }

    // Retrieves all resources
    abstract protected function getAll();

    // Retrieves a single resource by ID
    abstract protected function getOne($id);

    // Creates a new resource
    abstract protected function create();

    // Updates an existing resource
    abstract protected function update($id, $partial = false);

    // Deletes a resource by ID
    abstract protected function delete($id);

    // Gets the count of resources
    abstract protected function getCount();

    // Extracts pagination parameters from the request
    protected function getPaginationParams() {
        $params = [];

        $params['page'] = isset($this->request['query']['page']) ? (int)$this->request['query']['page'] : 1;
        $params['limit'] = isset($this->request['query']['limit']) ? (int)$this->request['query']['limit'] : 10;

        // Ensure valid values
        $params['page'] = $params['page'] < 1 ? 1 : $params['page'];
        $params['limit'] = $params['limit'] < 1 ? 10 : ($params['limit'] > 100 ? 100 : $params['limit']);

        return $params;
    }

    // Gets query parameters from the request excluding pagination parameters
    protected function getQueryParams() {
        $query = $this->request['query'] ?? [];

        // Remove pagination params
        unset($query['page']);
        unset($query['limit']);

        return $query;
    }

    // Retrieves the request body data
    protected function getBody() {
        return $this->request['body'] ?? [];
    }

    // Gets the current user ID from the request
    protected function getUserId() {
        return isset($this->request['user']) ? (int)$this->request['user']['id'] : null;
    }

    // Gets the current user type from the request
    protected function getUserType() {
        return isset($this->request['user']) ? $this->request['user']['user_type'] : null;
    }

    // Checks if the current user is an admin
    protected function isAdmin() {
        return $this->getUserType() === 'ADMIN';
    }

    // Validates that required fields are present in the data
    protected function validateRequired($data, $required) {
        $errors = [];

        foreach ($required as $field) {
            if (!isset($data[$field]) || empty($data[$field])) {
                $errors[] = "$field is required";
            }
        }

        return !empty($errors) ? $errors : false;
    }
}
