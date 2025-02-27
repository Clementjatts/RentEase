<?php
/**
 * PHPUnit Bootstrap File
 * 
 * Sets up the test environment for RentEase API tests
 */

// Load test environment variables
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

// Set error reporting to maximum during tests
error_reporting(E_ALL);
ini_set('display_errors', '1');

// Define the base path
define('BASE_PATH', dirname(__DIR__));

// Load the autoloader if using Composer
if (file_exists(BASE_PATH . '/vendor/autoload.php')) {
    require_once BASE_PATH . '/vendor/autoload.php';
}

// Load necessary API files
require_once BASE_PATH . '/config/Config.php';
require_once BASE_PATH . '/config/Database.php';
require_once BASE_PATH . '/utils/Logger.php';
require_once BASE_PATH . '/services/ResponseService.php';
require_once BASE_PATH . '/utils/JWT.php';
require_once BASE_PATH . '/middleware/AuthMiddleware.php';

// Load test helpers
require_once __DIR__ . '/TestCase.php';
require_once __DIR__ . '/ApiTestCase.php';

// Create test database if it doesn't exist
function setupTestDatabase() {
    try {
        // Connect to the MySQL server without selecting a database
        $pdo = new PDO(
            'mysql:host=' . getenv('DB_HOST'),
            getenv('DB_USER'),
            getenv('DB_PASS')
        );
        
        // Set the PDO error mode
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        
        // Check if test database exists, create if not
        $dbName = getenv('DB_NAME');
        $pdo->exec("CREATE DATABASE IF NOT EXISTS $dbName");
        
        // Select the test database
        $pdo->exec("USE $dbName");
        
        // Import schema
        $schemaFile = BASE_PATH . '/tests/fixtures/schema.sql';
        if (file_exists($schemaFile)) {
            $sql = file_get_contents($schemaFile);
            $pdo->exec($sql);
        }
        
        // Import test data
        $dataFile = BASE_PATH . '/tests/fixtures/test_data.sql';
        if (file_exists($dataFile)) {
            $sql = file_get_contents($dataFile);
            $pdo->exec($sql);
        }
        
        return true;
    } catch (PDOException $e) {
        echo "Database setup failed: " . $e->getMessage() . "\n";
        return false;
    }
}

// Try to set up the test database
setupTestDatabase();
