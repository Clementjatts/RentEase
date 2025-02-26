<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, Authorization, X-Requested-With');

require_once '../config/Database.php';

// Get posted data
$data = json_decode(file_get_contents("php://input"));

// Validate required fields
if(empty($data->username) || empty($data->password) || empty($data->email) || 
   empty($data->user_type) || empty($data->full_name) || empty($data->phone)) {
    http_response_code(400);
    echo json_encode([
        "message" => "Missing required fields"
    ]);
    exit;
}

try {
    $database = new Database();
    $db = $database->getConnection();
    
    // Check if username already exists (in admins table for now)
    if($data->user_type === 'ADMIN') {
        $query = "SELECT id FROM admins WHERE username = :username";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':username', $data->username);
        $stmt->execute();
        
        if($stmt->rowCount() > 0) {
            http_response_code(409); // Conflict
            echo json_encode([
                "message" => "Username already exists"
            ]);
            exit;
        }
        
        // Create new admin
        $query = "INSERT INTO admins (username, password) VALUES (:username, :password)";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':username', $data->username);
        $stmt->bindParam(':password', $data->password); // In production, use password_hash
        
        if($stmt->execute()) {
            $id = $db->lastInsertId();
            $token = bin2hex(random_bytes(16));
            
            $user = [
                "id" => $id,
                "username" => $data->username,
                "email" => $data->email,
                "user_type" => $data->user_type,
                "full_name" => $data->full_name,
                "phone" => $data->phone,
                "created_at" => date('Y-m-d H:i:s')
            ];
            
            http_response_code(201);
            echo json_encode([
                "token" => $token,
                "user" => $user,
                "message" => "Registration successful"
            ]);
        } else {
            http_response_code(500);
            echo json_encode([
                "message" => "Unable to register user"
            ]);
        }
    } else if($data->user_type === 'LANDLORD') {
        // Create a new landlord
        $query = "INSERT INTO landlords (name, contact) VALUES (:name, :contact)";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':name', $data->full_name);
        $stmt->bindParam(':contact', $data->email);
        
        if($stmt->execute()) {
            $id = $db->lastInsertId();
            $token = bin2hex(random_bytes(16));
            
            $user = [
                "id" => $id,
                "username" => $data->username,
                "email" => $data->email,
                "user_type" => $data->user_type,
                "full_name" => $data->full_name,
                "phone" => $data->phone,
                "created_at" => date('Y-m-d H:i:s')
            ];
            
            http_response_code(201);
            echo json_encode([
                "token" => $token,
                "user" => $user,
                "message" => "Registration successful"
            ]);
        } else {
            http_response_code(500);
            echo json_encode([
                "message" => "Unable to register landlord"
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
