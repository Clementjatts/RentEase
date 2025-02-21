<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');

require_once '../config/Database.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    
    $data = json_decode(file_get_contents("php://input"));
    
    if(!empty($data->user_id) && !empty($data->property_id)) {
        // First check if the property exists
        $check_query = "SELECT id FROM properties WHERE id = :property_id";
        $check_stmt = $db->prepare($check_query);
        $check_stmt->bindParam(':property_id', $data->property_id);
        $check_stmt->execute();
        
        if($check_stmt->rowCount() == 0) {
            http_response_code(404);
            echo json_encode(["status" => "error", "message" => "Property not found"]);
            exit;
        }
        
        $query = "INSERT INTO user_requests (user_id, property_id, message) VALUES (:user_id, :property_id, :message)";
        $stmt = $db->prepare($query);
        
        $user_id = $data->user_id;
        $property_id = $data->property_id;
        $message = $data->message ?? '';
        
        $stmt->bindParam(':user_id', $user_id);
        $stmt->bindParam(':property_id', $property_id);
        $stmt->bindParam(':message', $message);
        
        if($stmt->execute()) {
            // Get the landlord's contact information
            $contact_query = "SELECT l.name, l.contact 
                            FROM landlords l 
                            JOIN properties p ON l.id = p.landlord_id 
                            WHERE p.id = :property_id";
            $contact_stmt = $db->prepare($contact_query);
            $contact_stmt->bindParam(':property_id', $property_id);
            $contact_stmt->execute();
            $landlord = $contact_stmt->fetch(PDO::FETCH_ASSOC);
            
            http_response_code(201);
            echo json_encode([
                "status" => "success",
                "message" => "Request sent successfully",
                "landlord_contact" => [
                    "name" => $landlord['name'],
                    "contact" => $landlord['contact']
                ]
            ]);
        } else {
            http_response_code(503);
            echo json_encode(["status" => "error", "message" => "Unable to create request"]);
        }
    } else {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "User ID and Property ID are required"]);
    }
} catch(PDOException $e) {
    http_response_code(503);
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
