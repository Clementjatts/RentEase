<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: PUT');

require_once '../config/Database.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    
    $data = json_decode(file_get_contents("php://input"));
    
    if(!empty($data->id) && (!empty($data->name) || !empty($data->contact))) {
        $query = "UPDATE landlords SET ";
        $params = [];
        
        if(!empty($data->name)) {
            $params[] = "name = :name";
        }
        if(!empty($data->contact)) {
            $params[] = "contact = :contact";
        }
        
        $query .= implode(", ", $params);
        $query .= " WHERE id = :id";
        
        $stmt = $db->prepare($query);
        
        if(!empty($data->name)) {
            $stmt->bindParam(':name', $data->name);
        }
        if(!empty($data->contact)) {
            $stmt->bindParam(':contact', $data->contact);
        }
        $stmt->bindParam(':id', $data->id);
        
        if($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["status" => "success", "message" => "Landlord updated successfully"]);
        } else {
            http_response_code(503);
            echo json_encode(["status" => "error", "message" => "Unable to update landlord"]);
        }
    } else {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Incomplete data"]);
    }
} catch(PDOException $e) {
    http_response_code(503);
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
