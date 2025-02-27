<?php
/**
 * Validation Middleware
 * 
 * Validates request data against defined rules
 */
class ValidationMiddleware {
    private $response;
    
    /**
     * Constructor
     * 
     * @param ResponseService $response Response service
     */
    public function __construct($response) {
        $this->response = $response;
        
        // Load the validator class
        require_once __DIR__ . '/../utils/Validator.php';
    }
    
    /**
     * Validate request data
     * 
     * @param array $data Data to validate
     * @param array $rules Validation rules
     * @return bool|array True if validation passes, or data on success
     */
    public function validate($data, $rules) {
        $validator = new Validator();
        
        if (!$validator->validate($data, $rules)) {
            $this->response->error('Validation failed', 422, $validator->getErrors());
            return false;
        }
        
        return $data;
    }
    
    /**
     * Get JSON request data and validate it
     * 
     * @param array $rules Validation rules
     * @return bool|array True if validation passes, or data on success
     */
    public function validateJson($rules) {
        // Get JSON data from request
        $data = json_decode(file_get_contents('php://input'), true);
        
        if (!$data) {
            $this->response->error('Invalid JSON data', 400);
            return false;
        }
        
        return $this->validate($data, $rules);
    }
    
    /**
     * Validate URL parameters
     * 
     * @param array $params Parameters to validate
     * @param array $rules Validation rules
     * @return bool|array True if validation passes, or data on success
     */
    public function validateParams($params, $rules) {
        return $this->validate($params, $rules);
    }
    
    /**
     * Validate form data
     * 
     * @param array $rules Validation rules
     * @return bool|array True if validation passes, or data on success
     */
    public function validateForm($rules) {
        return $this->validate($_POST, $rules);
    }
}
