<?php
/**
 * Logger Utility
 * 
 * Simple logging utility for API requests and errors
 */
class Logger {
    private $log_dir;
    private $request_log;
    private $error_log;
    
    /**
     * Constructor
     */
    public function __construct() {
        // Set log directory
        $this->log_dir = __DIR__ . '/../logs';
        
        // Create log directory if it doesn't exist
        if (!is_dir($this->log_dir)) {
            mkdir($this->log_dir, 0777, true);
        }
        
        // Set log files
        $this->request_log = $this->log_dir . '/request.log';
        $this->error_log = $this->log_dir . '/error.log';
    }
    
    /**
     * Log a message to the request log
     * 
     * @param string $message Message to log
     */
    public function log($message) {
        $timestamp = date('Y-m-d H:i:s');
        $log_message = "[$timestamp] $message" . PHP_EOL;
        
        file_put_contents($this->request_log, $log_message, FILE_APPEND);
    }
    
    /**
     * Log an error message to the error log
     * 
     * @param string $message Error message to log
     */
    public function error($message) {
        $timestamp = date('Y-m-d H:i:s');
        $log_message = "[$timestamp] ERROR: $message" . PHP_EOL;
        
        file_put_contents($this->error_log, $log_message, FILE_APPEND);
    }
    
    /**
     * Log a debug message to the request log if debug is enabled
     * 
     * @param string $message Debug message to log
     */
    public function debug($message) {
        // We can add a debug flag check here later
        $debug_enabled = true;
        
        if ($debug_enabled) {
            $timestamp = date('Y-m-d H:i:s');
            $log_message = "[$timestamp] DEBUG: $message" . PHP_EOL;
            
            file_put_contents($this->request_log, $log_message, FILE_APPEND);
        }
    }
    
    /**
     * Log an API request with details
     * 
     * @param string $method HTTP method
     * @param string $path Request path
     * @param array $params Request parameters
     * @param string $ip Client IP address
     */
    public function logRequest($method, $path, $params = [], $ip = null) {
        $ip = $ip ?? $_SERVER['REMOTE_ADDR'] ?? 'unknown';
        $params_str = !empty($params) ? json_encode($params) : 'none';
        
        $message = "REQUEST: $method $path | IP: $ip | Params: $params_str";
        $this->log($message);
    }
    
    /**
     * Log an API request from request array
     * 
     * @param array $request Request data array
     */
    public function logRequestFromArray($request) {
        $method = $request['method'] ?? 'unknown';
        $path = $request['path'] ?? 'unknown';
        $params = $request['query'] ?? [];
        $ip = $_SERVER['REMOTE_ADDR'] ?? 'unknown';
        
        $this->logRequest($method, $path, $params, $ip);
    }
    
    /**
     * Log an API response
     * 
     * @param int $status_code HTTP status code
     * @param string $response Response body
     */
    public function logResponse($status_code, $response) {
        $truncated_response = substr($response, 0, 200);
        if (strlen($response) > 200) {
            $truncated_response .= '...';
        }
        
        $message = "RESPONSE: Status $status_code | Body: $truncated_response";
        $this->log($message);
    }
    
    /**
     * Log an error with additional context
     * 
     * @param string $message Error message
     * @param array $context Additional error context
     */
    public function logError($message, $context = []) {
        $error_info = $message;
        
        // Add context if available
        if (!empty($context)) {
            $context_str = json_encode($context);
            $error_info .= " | Context: $context_str";
        }
        
        $this->error($error_info);
    }
}
