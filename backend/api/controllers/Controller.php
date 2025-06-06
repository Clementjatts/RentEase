<?php
/**
 * Base Controller Class
 *
 * Provides common functionality for all controllers
 */
abstract class Controller {
    protected $db;
    protected $service;
    protected $request;
    protected $response;

    /**
     * Constructor
     *
     * @param PDO $db Database connection
     * @param ResponseService $service Response service
     * @param array $request Request data
     */
    public function __construct($db, $service, $request) {
        $this->db = $db;
        $this->service = $service;
        $this->request = $request;
    }

    /**
     * Process the request and route to the appropriate method
     *
     * @return array Response data
     */
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

    /**
     * Get all resources
     *
     * @return array
     */
    abstract protected function getAll();

    /**
     * Get one resource by ID
     *
     * @param int $id Resource ID
     * @return array
     */
    abstract protected function getOne($id);

    /**
     * Create a new resource
     *
     * @return array
     */
    abstract protected function create();

    /**
     * Update a resource
     *
     * @param int $id Resource ID
     * @param bool $partial Whether this is a partial update (PATCH)
     * @return array
     */
    abstract protected function update($id, $partial = false);

    /**
     * Delete a resource
     *
     * @param int $id Resource ID
     * @return array
     */
    abstract protected function delete($id);

    /**
     * Get count of resources
     *
     * @return array
     */
    abstract protected function getCount();

    /**
     * Get pagination parameters from the request
     *
     * @return array
     */
    protected function getPaginationParams() {
        $params = [];

        $params['page'] = isset($this->request['query']['page']) ? (int)$this->request['query']['page'] : 1;
        $params['limit'] = isset($this->request['query']['limit']) ? (int)$this->request['query']['limit'] : 10;

        // Ensure valid values
        $params['page'] = $params['page'] < 1 ? 1 : $params['page'];
        $params['limit'] = $params['limit'] < 1 ? 10 : ($params['limit'] > 100 ? 100 : $params['limit']);

        return $params;
    }

    /**
     * Get query parameters from the request, filtering out pagination params
     *
     * @return array
     */
    protected function getQueryParams() {
        $query = $this->request['query'] ?? [];

        // Remove pagination params
        unset($query['page']);
        unset($query['limit']);

        return $query;
    }

    /**
     * Get body data from the request
     *
     * @return array
     */
    protected function getBody() {
        return $this->request['body'] ?? [];
    }

    /**
     * Get user ID from the request (from JWT)
     *
     * @return int|null
     */
    protected function getUserId() {
        return isset($this->request['user']) ? (int)$this->request['user']['id'] : null;
    }

    /**
     * Get user type from the request (from JWT)
     *
     * @return string|null
     */
    protected function getUserType() {
        return isset($this->request['user']) ? $this->request['user']['user_type'] : null;
    }

    /**
     * Check if the current user is an admin
     *
     * @return bool
     */
    protected function isAdmin() {
        return $this->getUserType() === 'ADMIN';
    }

    /**
     * Validate required fields in data
     *
     * @param array $data Data to validate
     * @param array $required Required fields
     * @return array|false Validation errors or false if valid
     */
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
