<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
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

// Get posted data
$data = json_decode(file_get_contents("php://input"));

if(empty($data->current_password) || empty($data->new_password)) {
    http_response_code(400);
    echo json_encode([
        "message" => "Missing required fields"
    ]);
    exit;
}

try {
    $database = new Database();
    $db = $database->getConnection();
    
    // First verify the current password
    $query = "SELECT id FROM admins WHERE username = :username AND password = :password";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':username', $username);
    $stmt->bindParam(':password', $data->current_password);
    $stmt->execute();
    
    if($stmt->rowCount() > 0) {
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        $userId = $row['id'];
        
        // Update the password
        $query = "UPDATE admins SET password = :new_password WHERE id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':new_password', $data->new_password);
        $stmt->bindParam(':id', $userId);
        
        if($stmt->execute()) {
            http_response_code(200);
            echo json_encode([
                "message" => "Password updated successfully"
            ]);
        } else {
            http_response_code(500);
            echo json_encode([
                "message" => "Failed to update password"
            ]);
        }
    } else {
        http_response_code(401);
        echo json_encode([
            "message" => "Current password is incorrect"
        ]);
    }
} catch(PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "message" => "Database error: " . $e->getMessage()
    ]);
}
