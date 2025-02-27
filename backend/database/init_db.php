<?php
/**
 * Database initialization script
 * Run this script to create or update the database schema
 */

require_once __DIR__ . '/../api/config/Database.php';

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
            $pdo->exec($statement);
            echo "Executed: " . substr($statement, 0, 50) . "...\n";
        }
    }
    
    echo "Database initialized successfully!\n";
    echo "Checking tables...\n";
    
    // List all tables
    $tables_query = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name";
    $tables_stmt = $pdo->query($tables_query);
    $tables = $tables_stmt->fetchAll(PDO::FETCH_COLUMN);
    
    echo "Tables in database:\n";
    foreach ($tables as $table) {
        echo "- $table\n";
        
        // Get table structure
        $struct_query = "PRAGMA table_info($table)";
        $struct_stmt = $pdo->query($struct_query);
        $columns = $struct_stmt->fetchAll(PDO::FETCH_ASSOC);
        
        echo "  Columns:\n";
        foreach ($columns as $column) {
            echo "  - {$column['name']} ({$column['type']})\n";
        }
        
        // Count rows
        $count_query = "SELECT COUNT(*) FROM $table";
        $count_stmt = $pdo->query($count_query);
        $count = $count_stmt->fetchColumn();
        
        echo "  Rows: $count\n";
    }
    
    echo "\nDatabase ready for use.\n";
    
} catch (PDOException $e) {
    die("Database error: " . $e->getMessage() . "\n");
}
