<?php
/**
 * Test Database Connection Script
 */

// Load environment variables from .env.test
$envFile = __DIR__ . '/.env.test';
if (file_exists($envFile)) {
    $lines = file($envFile, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        if (strpos($line, '=') !== false && strpos($line, '#') !== 0) {
            list($key, $value) = explode('=', $line, 2);
            $key = trim($key);
            $value = trim($value);
            putenv("$key=$value");
        }
    }
}

echo "Attempting to connect to MySQL...\n";
echo "Host: " . getenv('DB_HOST') . "\n";
echo "Database: " . getenv('DB_NAME') . "\n";
echo "User: " . getenv('DB_USER') . "\n";
echo "Password: " . str_repeat('*', strlen(getenv('DB_PASS'))) . "\n\n";

try {
    // Connect to the MySQL server without selecting a database
    $pdo = new PDO(
        'mysql:host=' . getenv('DB_HOST'),
        getenv('DB_USER'),
        getenv('DB_PASS')
    );
    
    // Set the PDO error mode
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    echo "Successfully connected to MySQL server!\n";
    
    // Check if test database exists, create if not
    $dbName = getenv('DB_NAME');
    echo "Checking if database '$dbName' exists...\n";
    
    $result = $pdo->query("SHOW DATABASES LIKE '$dbName'")->fetch();
    if (!$result) {
        echo "Database '$dbName' does not exist. Creating it...\n";
        $pdo->exec("CREATE DATABASE IF NOT EXISTS $dbName");
        echo "Database created successfully!\n";
    } else {
        echo "Database '$dbName' already exists.\n";
    }
    
    // Select the test database
    echo "Using database '$dbName'...\n";
    $pdo->exec("USE $dbName");
    
    echo "Connection test completed successfully!\n";
    
} catch (PDOException $e) {
    echo "Connection failed: " . $e->getMessage() . "\n";
    echo "Please check your database credentials in .env.test file.\n";
}
