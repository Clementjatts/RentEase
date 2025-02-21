<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once '../config/Database.php';
require_once '../utils/Auth.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    
    // Require admin authentication
    $auth = new Auth($db);
    $admin_id = $auth->requireAuth();
    
    $data = json_decode(file_get_contents("php://input"));
    
    if(!empty($data->name) && !empty($data->contact)) {
        $query = "INSERT INTO landlords (name, contact, admin_id) VALUES (:name, :contact, :admin_id)";
        $stmt = $db->prepare($query);
        
        $name = $data->name;
        $contact = $data->contact;
        
        $stmt->bindParam(':name', $name);
        $stmt->bindParam(':contact', $contact);
        $stmt->bindParam(':admin_id', $admin_id);
        
        if($stmt->execute()) {
            http_response_code(201);
            echo json_encode([
                "status" => "success", 
                "message" => "Landlord created successfully",
                "id" => $db->lastInsertId()
            ]);
        } else {
            http_response_code(503);
            echo json_encode(["status" => "error", "message" => "Unable to create landlord"]);
        }
    } else {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Incomplete data"]);
    }
} catch(PDOException $e) {
    http_response_code(503);
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
