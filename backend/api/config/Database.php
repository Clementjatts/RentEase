<?php
/**
 * Database Connection Class
 * 
 * Handles database connection and initialization
 */
class Database {
    private $db;
    private $dbPath;

    public function __construct() {
        try {
            // Determine the correct database path based on environment
            // For Docker, the path is /var/www/database/rentease.db
            // For local development, use a relative path
            $db_path = getenv('SQLITE_DATABASE');
            if (!$db_path) {
                $db_path = dirname(dirname(dirname(__FILE__))) . '/database/rentease.db';
            }
            
            $this->dbPath = $db_path;
            
            // Check if directory exists and is writable
            $dir = dirname($db_path);
            if (!is_dir($dir)) {
                mkdir($dir, 0777, true);
            }
            
            $this->db = new PDO('sqlite:' . $db_path);
            $this->db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            
            // Note: We don't execute the schema here anymore
            // Use the init_db.php script to initialize the database properly
        } catch(PDOException $e) {
            // Log error but don't expose details to client
            error_log("Database Connection Error: " . $e->getMessage());
            throw new Exception("Database connection error");
        }
    }

    public function getConnection() {
        return $this->db;
    }
    
    // Added method to get DSN for debugging
    public function getDatabaseInfo() {
        return [
            'path' => $this->dbPath,
            'exists' => file_exists($this->dbPath),
            'size' => file_exists($this->dbPath) ? filesize($this->dbPath) : 0
        ];
    }
}
