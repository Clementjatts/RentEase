<?php
// Global configuration settings for the RentEase API
class Config {
    // API settings
    public static $api_version = '1.0.0';
    public static $debug_mode = false;

    // Pagination defaults
    public static $default_page_size = 20;
    public static $max_page_size = 100;

    // Upload settings
    public static $upload_dir = __DIR__ . '/../../uploads/';
    public static $allowed_extensions = ['jpg', 'jpeg', 'png', 'pdf'];
    public static $max_file_size = 5242880; // 5MB in bytes

    // Base URL for the API (used for generating image URLs)
    public static $base_url = 'http://localhost:8000';

    // Security settings
    public static $cors_origins = ['*'];

    // Retrieves a configuration value by key
    public static function get($key) {
        if (property_exists(self::class, $key)) {
            return self::$$key;
        }
        return null;
    }

}
