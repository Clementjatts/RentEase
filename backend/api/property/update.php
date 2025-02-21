<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: PUT');

require_once '../config/Database.php';
require_once '../utils/Auth.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    
    $data = json_decode(file_get_contents("php://input"));
    
    if(!empty($data->id)) {
        // First get the property to check ownership
        $check_query = "SELECT landlord_id FROM properties WHERE id = :id";
        $check_stmt = $db->prepare($check_query);
        $check_stmt->bindParam(':id', $data->id);
        $check_stmt->execute();
        
        if($check_stmt->rowCount() == 0) {
            http_response_code(404);
            echo json_encode(["status" => "error", "message" => "Property not found"]);
            exit;
        }
        
        $property = $check_stmt->fetch(PDO::FETCH_ASSOC);
        
        // Require landlord authentication
        $auth = new Auth($db);
        $auth->requireLandlordAuth($property['landlord_id']);
        
        $query = "UPDATE properties SET ";
        $params = [];
        $values = [];
        
        if(!empty($data->title)) {
            $params[] = "title = :title";
            $values[':title'] = $data->title;
        }
        if(isset($data->description)) {
            $params[] = "description = :description";
            $values[':description'] = $data->description;
        }
        
        if(empty($params)) {
            http_response_code(400);
            echo json_encode(["status" => "error", "message" => "No fields to update"]);
            exit;
        }
        
        $query .= implode(", ", $params);
        $query .= " WHERE id = :id";
        $values[':id'] = $data->id;
        
        $stmt = $db->prepare($query);
        
        if($stmt->execute($values)) {
            http_response_code(200);
            echo json_encode(["status" => "success", "message" => "Property updated successfully"]);
        } else {
            http_response_code(503);
            echo json_encode(["status" => "error", "message" => "Unable to update property"]);
        }
    } else {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Property ID is required"]);
    }
} catch(PDOException $e) {
    http_response_code(503);
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
