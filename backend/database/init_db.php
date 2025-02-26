<?php
/**
 * Database initialization script
 * Run this script to create or update the database schema
 */

require_once '../api/config/Database.php';

try {
    $db_file = __DIR__ . '/rentease.db';
    $schema_file = __DIR__ . '/schema.sql';
    
    echo "Initializing RentEase database...\n";
    
    // Check if schema file exists
    if (!file_exists($schema_file)) {
        die("Error: Schema file not found at $schema_file\n");
    }
    
    // Read schema SQL
    $schema_sql = file_get_contents($schema_file);
    if (!$schema_sql) {
        die("Error: Could not read schema file\n");
    }
    
    // Initialize database connection
    $database = new Database();
    $pdo = $database->getConnection();
    
    // Execute schema SQL as multiple statements
    $statements = explode(';', $schema_sql);
    foreach ($statements as $statement) {
        $statement = trim($statement);
        if (!empty($statement)) {
            try {
                $pdo->exec($statement);
            } catch (PDOException $e) {
                echo "Warning: " . $e->getMessage() . "\n";
                // Continue with next statement even if this one fails
            }
        }
    }
    
    echo "Database initialization completed successfully!\n";
    echo "Database location: $db_file\n";
    
} catch (Exception $e) {
    die("Error: " . $e->getMessage() . "\n");
}
