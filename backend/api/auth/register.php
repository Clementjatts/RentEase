<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Access-Control-Allow-Headers, Content-Type, Access-Control-Allow-Methods, Authorization, X-Requested-With');

require_once '../config/Database.php';

// Get posted data
$data = json_decode(file_get_contents("php://input"));

// Debug: Log received data
error_log("Register request data: " . print_r($data, true));

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
    
    // Check if username already exists
    $query = "SELECT COUNT(*) as count FROM users WHERE username = :username";
    $stmt = $db->prepare($query);
    $stmt->bindParam(':username', $data->username);
    $stmt->execute();
    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    if($row['count'] > 0) {
        http_response_code(400);
        echo json_encode([
            "message" => "Username already exists"
        ]);
        exit;
    }

    // Check and sanitize user_type
    $userType = strtoupper(trim($data->user_type));

    if($userType === 'ADMIN') {
        // Create a new user (admin)
        $query = "INSERT INTO users (username, password, email, user_type, full_name, phone) VALUES (:username, :password, :email, :user_type, :full_name, :phone)";
        $stmt = $db->prepare($query);
        $password_hash = password_hash($data->password, PASSWORD_BCRYPT);
        $stmt->bindParam(':username', $data->username);
        $stmt->bindParam(':password', $password_hash);
        $stmt->bindParam(':email', $data->email);
        $stmt->bindParam(':user_type', $userType);
        $stmt->bindParam(':full_name', $data->full_name);
        $stmt->bindParam(':phone', $data->phone);
        
        if($stmt->execute()) {
            $id = $db->lastInsertId();
            $token = bin2hex(random_bytes(16));
            
            $user = [
                "id" => $id,
                "username" => $data->username,
                "email" => $data->email,
                "user_type" => $userType,
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
    } else if($userType === 'LANDLORD') {
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
                "user_type" => $userType,
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
