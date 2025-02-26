<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, Authorization, X-Requested-With');

require_once '../config/Database.php';

// Check for Authorization header
$auth_header = isset($_SERVER['HTTP_AUTHORIZATION']) ? $_SERVER['HTTP_AUTHORIZATION'] : '';
if (!$auth_header) {
    // Check if PHP-CGI
    $auth_header = isset($_SERVER['REDIRECT_HTTP_AUTHORIZATION']) ? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] : '';
}

if (!$auth_header) {
    http_response_code(401);
    echo json_encode([
        "message" => "Authorization header required"
    ]);
    exit;
}

// Extract credentials from Basic Auth
$credentials = explode(':', base64_decode(substr($auth_header, 6)));
if (count($credentials) != 2) {
    http_response_code(401);
    echo json_encode([
        "message" => "Invalid authorization header"
    ]);
    exit;
}

list($username, $password) = $credentials;

try {
    $database = new Database();
    $db = $database->getConnection();
    
    // First check if it's an admin
    $query = "SELECT id FROM admins WHERE username = :username AND password = :password";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':username', $username);
    $stmt->bindParam(':password', $password);
    $stmt->execute();
    
    if($stmt->rowCount() > 0) {
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        $user = [
            "id" => $row['id'],
            "username" => $username,
            "email" => "admin@rentease.com", // Mocked for this example
            "user_type" => "ADMIN",
            "full_name" => "Admin User", // Mocked for this example
            "phone" => "+1234567890", // Mocked for this example
            "created_at" => date('Y-m-d H:i:s')
        ];
        
        http_response_code(200);
        echo json_encode($user);
    } else {
        // Check if it's a landlord
        $query = "SELECT id, name, contact FROM landlords WHERE name = :username LIMIT 1";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':username', $username);
        $stmt->execute();
        
        if($stmt->rowCount() > 0) {
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            $user = [
                "id" => $row['id'],
                "username" => $row['name'],
                "email" => $row['contact'],
                "user_type" => "LANDLORD",
                "full_name" => $row['name'],
                "phone" => "", // Not available in the current schema
                "created_at" => date('Y-m-d H:i:s')
            ];
            
            http_response_code(200);
            echo json_encode($user);
        } else {
            http_response_code(401);
            echo json_encode([
                "message" => "Invalid credentials"
            ]);
        }
    }
} catch(PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "message" => "Database error: " . $e->getMessage()
    ]);
}
