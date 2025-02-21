<?php
class Database {
    private $db;

    public function __construct() {
        try {
            $this->db = new PDO('sqlite:/var/www/database/rentease.db');
            $this->db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            
            // Create tables if they don't exist
            $schema = file_get_contents('/var/www/database/schema.sql');
            $this->db->exec($schema);
        } catch(PDOException $e) {
            echo "Connection Error: " . $e->getMessage();
        }
    }

    public function getConnection() {
        return $this->db;
    }
}
