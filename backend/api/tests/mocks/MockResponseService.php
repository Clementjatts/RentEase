<?php
/**
 * Mock Response Service for Testing
 * 
 * This class extends ResponseService and overrides methods that would normally
 * interact with PHP's header and output functions, making it suitable for testing.
 */
class MockResponseService {
    /**
     * Track the status code that would have been set
     */
    public $statusCode = 200;
    
    /**
     * Track the response that would have been sent
     */
    public $response = null;
    
    /**
     * Override http_response_code to prevent actual header setting
     */
    protected function setStatusCode($code) {
        $this->statusCode = $code;
        return $code;
    }
    
    /**
     * Override json output to capture response instead of sending it
     */
    protected function outputJson($data) {
        $this->response = $data;
        return $data;
    }
    
    /**
     * Success response
     */
    public function success($message, $data = null, $status_code = 200, $count = null, $pagination = null) {
        $this->setStatusCode($status_code);
        
        $response = [
            'status' => 'success',
            'message' => $message
        ];
        
        if ($data !== null) {
            $response['data'] = $data;
        }
        
        if ($count !== null) {
            $response['count'] = $count;
        }
        
        if ($pagination !== null) {
            $response['pagination'] = $pagination;
        }
        
        $this->outputJson($response);
        return $response;
    }
    
    /**
     * Error response
     */
    public function error($message, $errors = null, $status_code = 400) {
        $this->setStatusCode($status_code);
        
        $response = [
            'status' => 'error',
            'message' => $message
        ];
        
        if ($errors !== null) {
            $response['errors'] = $errors;
        }
        
        $this->outputJson($response);
        return $response;
    }
    
    /**
     * Created response
     */
    public function created($message, $data = null) {
        return $this->success($message, $data, 201);
    }
    
    /**
     * Bad request response
     */
    public function badRequest($message = 'Bad Request', $errors = null) {
        return $this->error($message, $errors, 400);
    }
    
    /**
     * Unauthorized response
     */
    public function unauthorized($message = 'Unauthorized', $errors = null) {
        return $this->error($message, $errors, 401);
    }
    
    /**
     * Forbidden response
     */
    public function forbidden($message = 'Access forbidden', $errors = null) {
        return $this->error($message, $errors, 403);
    }
    
    /**
     * Not found response
     */
    public function notFound($message = 'Resource not found', $errors = null) {
        return $this->error($message, $errors, 404);
    }
    
    /**
     * Method not allowed response
     */
    public function methodNotAllowed($message = 'Method not allowed', $errors = null) {
        return $this->error($message, $errors, 405);
    }
    
    /**
     * Conflict response
     */
    public function conflict($message = 'Resource conflict', $errors = null) {
        return $this->error($message, $errors, 409);
    }
    
    /**
     * Server error response
     */
    public function serverError($message = 'Internal server error', $errors = null) {
        return $this->error($message, $errors, 500);
    }
}
