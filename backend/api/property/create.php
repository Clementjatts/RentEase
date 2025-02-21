<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once '../config/Database.php';
require_once '../utils/Auth.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    
    $data = json_decode(file_get_contents("php://input"));
    
    if(!empty($data->title) && !empty($data->landlord_id)) {
        // Require landlord authentication
        $auth = new Auth($db);
        $auth->requireLandlordAuth($data->landlord_id);
        
        $query = "INSERT INTO properties (title, description, landlord_id) VALUES (:title, :description, :landlord_id)";
        $stmt = $db->prepare($query);
        
        $title = $data->title;
        $description = $data->description ?? '';
        $landlord_id = $data->landlord_id;
        
        $stmt->bindParam(':title', $title);
        $stmt->bindParam(':description', $description);
        $stmt->bindParam(':landlord_id', $landlord_id);
        
        if($stmt->execute()) {
            http_response_code(201);
            echo json_encode([
                "status" => "success",
                "message" => "Property created successfully",
                "id" => $db->lastInsertId()
            ]);
        } else {
            http_response_code(503);
            echo json_encode(["status" => "error", "message" => "Unable to create property"]);
        }
    } else {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Title and landlord_id are required"]);
    }
} catch(PDOException $e) {
    http_response_code(503);
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
