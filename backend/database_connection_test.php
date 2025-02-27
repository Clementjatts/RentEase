<?php
/**
 * Comprehensive Database Connection Test
 * Tests all the different ways the database is accessed
 */

echo "=== RentEase Database Connection Test ===\n";
echo "Run at: " . date('Y-m-d H:i:s') . "\n\n";

// Test 1: Direct SQLite connection
echo "TEST 1: Direct SQLite Connection\n";
$db_path = __DIR__ . '/database/rentease.db';
echo "- Database path: $db_path\n";
echo "- File exists: " . (file_exists($db_path) ? "Yes" : "No") . "\n";
echo "- File size: " . (file_exists($db_path) ? filesize($db_path) . " bytes" : "N/A") . "\n";

try {
    $pdo_direct = new PDO('sqlite:' . $db_path);
    $pdo_direct->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    echo "- Connection: SUCCESS\n";
    
    // Count properties
    $stmt = $pdo_direct->query("SELECT COUNT(*) FROM properties");
    $count = $stmt->fetchColumn();
    echo "- Properties count: $count\n";
    
    // List properties
    echo "- Properties in database:\n";
    $stmt = $pdo_direct->query("SELECT id, title FROM properties");
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        echo "  - ID: {$row['id']}, Title: {$row['title']}\n";
    }
} catch (PDOException $e) {
    echo "- Connection FAILED: " . $e->getMessage() . "\n";
}
echo "\n";

// Test 2: Database class test
echo "TEST 2: Database Class Connection\n";
require_once 'api/config/Database.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    $db_info = $database->getDatabaseInfo();
    
    echo "- DB Info: " . json_encode($db_info, JSON_PRETTY_PRINT) . "\n";
    
    // Test a simple query using the Database class
    $stmt = $db->query("SELECT COUNT(*) FROM properties");
    $count = $stmt->fetchColumn();
    echo "- Properties count via Database class: $count\n";
    
    // Test a prepared statement with positional parameters
    echo "- Testing property lookup with positional parameters:\n";
    $stmt = $db->prepare("SELECT * FROM properties WHERE id = ?");
    
    // Test all property IDs 1-5
    for ($i = 1; $i <= 5; $i++) {
        $stmt->execute([$i]);
        $property = $stmt->fetch(PDO::FETCH_ASSOC);
        echo "  - Property ID $i: " . ($property ? "Found '{$property['title']}'" : "Not found") . "\n";
    }
    
    // Test a prepared statement with named parameters
    echo "- Testing property lookup with named parameters:\n";
    $stmt = $db->prepare("SELECT * FROM properties WHERE id = :id");
    
    // Test all property IDs 1-5
    for ($i = 1; $i <= 5; $i++) {
        $stmt->bindValue(':id', $i, PDO::PARAM_INT);
        $stmt->execute();
        $property = $stmt->fetch(PDO::FETCH_ASSOC);
        echo "  - Property ID $i: " . ($property ? "Found '{$property['title']}'" : "Not found") . "\n";
    }
    
} catch (PDOException $e) {
    echo "- Connection FAILED: " . $e->getMessage() . "\n";
}
echo "\n";

// Test 3: Test connection via models
echo "TEST 3: Model Logic\n";

try {
    $database = new Database();
    $db = $database->getConnection();
    
    // Test property ID 1 - direct query
    $stmt = $db->query("SELECT * FROM properties WHERE id = 1");
    $property_direct = $stmt->fetch(PDO::FETCH_ASSOC);
    echo "- Property ID 1 (direct query): " . ($property_direct ? "Found" : "Not found") . "\n";
    
    // Test property ID 1 - prepared statement with positional parameters
    $stmt = $db->prepare("SELECT * FROM properties WHERE id = ?");
    $property_id = 1;
    $stmt->execute([$property_id]);
    $property_prepared = $stmt->fetch(PDO::FETCH_ASSOC);
    echo "- Property ID 1 (prepared with ?): " . ($property_prepared ? "Found" : "Not found") . "\n";
    
    // Test property ID 1 - prepared statement with named parameters
    $stmt = $db->prepare("SELECT * FROM properties WHERE id = :id");
    $property_id = 1;
    $stmt->bindParam(':id', $property_id);
    $stmt->execute();
    $property_named = $stmt->fetch(PDO::FETCH_ASSOC);
    echo "- Property ID 1 (prepared with :id): " . ($property_named ? "Found" : "Not found") . "\n";
    
    // Test existence check
    $property_id = 1;
    $check_query = "SELECT id FROM properties WHERE id = ?";
    $check_stmt = $db->prepare($check_query);
    $check_stmt->execute([$property_id]);
    $record_exists = $check_stmt->fetch(PDO::FETCH_ASSOC);
    
    if(!$record_exists) {
        echo "- Property existence check: Property not found\n";
    } else {
        echo "- Property existence check: Property found\n";
    }
    
} catch(PDOException $e) {
    echo "- ERROR: " . $e->getMessage() . "\n";
}

echo "\n=== Test Completed ===\n";
