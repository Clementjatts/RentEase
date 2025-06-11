<?php
// Database connection class that handles SQLite database initialization and connection
class Database {
    private $db;
    private $dbPath;

    // Initializes database connection with automatic path detection for Docker and local environments
    public function __construct() {
        try {
            // Check if running in Docker container
            if (file_exists('/var/www/database/rentease.db')) {
                // Docker container path
                $db_path = '/var/www/database/rentease.db';
            } else {
                // Local development path
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

        } catch(PDOException $e) {
            // Log error but don't expose details to client
            error_log("Database Connection Error: " . $e->getMessage());
            throw new Exception("Database connection error");
        }
    }

    // Returns the PDO database connection instance
    public function getConnection() {
        return $this->db;
    }

    // Returns database information for debugging purposes
    public function getDatabaseInfo() {
        return [
            'path' => $this->dbPath,
            'exists' => file_exists($this->dbPath),
            'size' => file_exists($this->dbPath) ? filesize($this->dbPath) : 0
        ];
    }
}
