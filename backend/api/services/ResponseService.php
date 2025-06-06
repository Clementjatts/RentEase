<?php
/**
 * Response Service
 * 
 * Handles standardized API responses
 */
class ResponseService {
    /**
     * Send a success response
     * 
     * @param string $message Optional success message
     * @param mixed $data The data to return
     * @param int $status_code HTTP status code
     * @param int $count Optional count for collections
     * @param array $pagination Optional pagination info
     */
    public function success($message, $data = null, $status_code = 200, $count = null, $pagination = null) {
        http_response_code($status_code);
        
        $response = [
            'status' => 'success',
            'message' => $message,
            'success' => true
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
        
        echo json_encode($response);
        exit();
    }
    
    /**
     * Send an error response
     * 
     * @param string $message Error message
     * @param int $status_code HTTP status code
     * @param array $errors Optional detailed errors
     */
    public function error($message, $status_code = 400, $errors = null) {
        http_response_code($status_code);
        
        $response = [
            'status' => 'error',
            'message' => $message,
            'success' => false
        ];
        
        if ($errors !== null) {
            $response['errors'] = $errors;
        }
        
        echo json_encode($response);
        exit();
    }
    
    /**
     * Send a created response
     * 
     * @param mixed $data The created resource
     * @param string $message Optional message
     */
    public function created($data, $message = 'Resource created successfully') {
        $this->success($message, $data, 201);
    }
    
    /**
     * Send a no content response
     */
    public function noContent() {
        http_response_code(204);
        exit();
    }
    
    /**
     * Generate pagination metadata
     * 
     * @param int $total Total number of items
     * @param int $page Current page number
     * @param int $limit Items per page
     * @return array Pagination metadata
     */
    public function paginate($total, $page, $limit) {
        $total_pages = ceil($total / $limit);
        
        return [
            'total' => (int)$total,
            'per_page' => (int)$limit,
            'current_page' => (int)$page,
            'last_page' => (int)$total_pages,
            'from' => (int)(($page - 1) * $limit + 1),
            'to' => (int)min($page * $limit, $total)
        ];
    }
    
    /**
     * Send a bad request response
     * 
     * @param string $message Error message
     * @param array $errors Optional detailed errors
     * @return null
     */
    public function badRequest($message = 'Bad Request', $errors = null) {
        $this->error($message, 400, $errors);
    }
    
    /**
     * Send an unauthorized response
     * 
     * @param string $message Error message
     * @param array $errors Optional detailed errors
     * @return null
     */
    public function unauthorized($message = 'Unauthorized', $errors = null) {
        $this->error($message, 401, $errors);
    }
    
    /**
     * Send a forbidden response
     * 
     * @param string $message Error message
     * @param array $errors Optional detailed errors
     * @return null
     */
    public function forbidden($message = 'Forbidden', $errors = null) {
        $this->error($message, 403, $errors);
    }
    
    /**
     * Send a not found response
     * 
     * @param string $message Error message
     * @param array $errors Optional detailed errors
     * @return null
     */
    public function notFound($message = 'Not Found', $errors = null) {
        $this->error($message, 404, $errors);
    }
    
    /**
     * Send a method not allowed response
     * 
     * @param string $message Error message
     * @param array $errors Optional detailed errors
     * @return null
     */
    public function methodNotAllowed($message = 'Method Not Allowed', $errors = null) {
        $this->error($message, 405, $errors);
    }
    
    /**
     * Send a conflict response
     * 
     * @param string $message Error message
     * @param array $errors Optional detailed errors
     * @return null
     */
    public function conflict($message = 'Conflict', $errors = null) {
        $this->error($message, 409, $errors);
    }
    
    /**
     * Send a server error response
     * 
     * @param string $message Error message
     * @param array $errors Optional detailed errors
     * @return null
     */
    public function serverError($message = 'Internal Server Error', $errors = null) {
        $this->error($message, 500, $errors);
    }
}
