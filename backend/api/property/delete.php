<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: DELETE');

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
        
        // First delete any user requests for this property
        $query = "DELETE FROM user_requests WHERE property_id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $data->id);
        $stmt->execute();
        
        // Then delete the property
        $query = "DELETE FROM properties WHERE id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $data->id);
        
        if($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["status" => "success", "message" => "Property deleted successfully"]);
        } else {
            http_response_code(503);
            echo json_encode(["status" => "error", "message" => "Unable to delete property"]);
        }
    } else {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Property ID is required"]);
    }
} catch(PDOException $e) {
    http_response_code(503);
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
