<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, Authorization, X-Requested-With');

require_once '../config/Database.php';

// Get posted data
$data = json_decode(file_get_contents("php://input"));

if(empty($data->username) || empty($data->password) || empty($data->user_type)) {
    http_response_code(400);
    echo json_encode([
        "message" => "Missing required fields"
    ]);
    exit;
}

try {
    $database = new Database();
    $db = $database->getConnection();
    
    // For the purpose of this example, we'll just check against the admins table
    // In a real app, you would have a users table with user_type field
    if($data->user_type === 'ADMIN') {
        $query = "SELECT id, username FROM admins WHERE username = :username AND password = :password";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':username', $data->username);
        $stmt->bindParam(':password', $data->password); // Note: In production, use password_hash and password_verify
        $stmt->execute();
        
        if($stmt->rowCount() > 0) {
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            $token = bin2hex(random_bytes(16)); // Generate a simple token
            
            // In a real app, you'd store this token in a database
            $user = [
                "id" => $row['id'],
                "username" => $row['username'],
                "email" => "admin@rentease.com", // Mocked for this example
                "user_type" => "ADMIN",
                "full_name" => "Admin User", // Mocked for this example
                "phone" => "+1234567890", // Mocked for this example
                "created_at" => date('Y-m-d H:i:s')
            ];
            
            http_response_code(200);
            echo json_encode([
                "token" => $token,
                "user" => $user,
                "message" => "Login successful"
            ]);
        } else {
            http_response_code(401);
            echo json_encode([
                "message" => "Invalid credentials"
            ]);
        }
    } else if($data->user_type === 'LANDLORD') {
        // For landlords, we need to check if there's a landlord with matching credentials
        // Since we don't have actual user authentication for landlords in the schema,
        // this is a simplified example
        $query = "SELECT id, name, contact FROM landlords WHERE name = :username LIMIT 1";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':username', $data->username);
        $stmt->execute();
        
        if($stmt->rowCount() > 0) {
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            $token = bin2hex(random_bytes(16));
            
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
            echo json_encode([
                "token" => $token,
                "user" => $user,
                "message" => "Login successful"
            ]);
        } else {
            http_response_code(401);
            echo json_encode([
                "message" => "Invalid credentials"
            ]);
        }
    } else {
        http_response_code(400);
        echo json_encode([
            "message" => "Unsupported user type"
        ]);
    }
} catch(PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "message" => "Database error: " . $e->getMessage()
    ]);
}
