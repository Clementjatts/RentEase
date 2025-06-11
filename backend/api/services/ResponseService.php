<?php
// Response service that handles standardized API responses with consistent formatting
class ResponseService {
    // Sends a success response with optional data and pagination
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

    // Sends an error response with optional detailed error information
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

    // Sends a created response for successful resource creation
    public function created($data, $message = 'Resource created successfully') {
        $this->success($message, $data, 201);
    }

    // Sends a no content response for successful operations without data
    public function noContent() {
        http_response_code(204);
        exit();
    }

    // Generates pagination metadata for paginated responses
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

    // Sends a 400 Bad Request response
    public function badRequest($message = 'Bad Request', $errors = null) {
        $this->error($message, 400, $errors);
    }

    // Sends a 401 Unauthorized response
    public function unauthorized($message = 'Unauthorized', $errors = null) {
        $this->error($message, 401, $errors);
    }

    // Sends a 403 Forbidden response
    public function forbidden($message = 'Forbidden', $errors = null) {
        $this->error($message, 403, $errors);
    }

    // Sends a 404 Not Found response
    public function notFound($message = 'Not Found', $errors = null) {
        $this->error($message, 404, $errors);
    }

    // Sends a 405 Method Not Allowed response
    public function methodNotAllowed($message = 'Method Not Allowed', $errors = null) {
        $this->error($message, 405, $errors);
    }

    // Sends a 409 Conflict response
    public function conflict($message = 'Conflict', $errors = null) {
        $this->error($message, 409, $errors);
    }

    // Sends a 500 Internal Server Error response
    public function serverError($message = 'Internal Server Error', $errors = null) {
        $this->error($message, 500, $errors);
    }
}
