<?php
/**
 * Global Configuration Settings
 */
class Config {
    // API settings
    public static $api_version = '1.0.0';
    public static $debug_mode = true; // Set to false in production
    
    // JWT Authentication settings
    public static $jwt_key = 'your-secret-key-here'; // Change this in production
    public static $jwt_expiry = 86400; // 24 hours in seconds
    
    // Pagination defaults
    public static $default_page_size = 20;
    public static $max_page_size = 100;
    
    // Upload settings
    public static $upload_dir = __DIR__ . '/../../uploads/';
    public static $allowed_extensions = ['jpg', 'jpeg', 'png', 'pdf'];
    public static $max_file_size = 5242880; // 5MB in bytes
    
    // Security settings
    public static $cors_origins = ['*']; // Restrict in production
    
    // Get a configuration value
    public static function get($key) {
        if (property_exists(self::class, $key)) {
            return self::$$key;
        }
        return null;
    }
    
    /**
     * Get JWT configuration
     * 
     * @return array JWT configuration settings
     */
    public static function getJwtConfig() {
        return [
            'secret_key' => self::$jwt_key,
            'expiration' => self::$jwt_expiry,
            'algorithm' => 'HS256'
        ];
    }
}
