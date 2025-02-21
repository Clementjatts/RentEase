<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: DELETE');

require_once '../config/Database.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    
    $data = json_decode(file_get_contents("php://input"));
    
    if(!empty($data->id)) {
        // First delete all properties associated with this landlord
        $query = "DELETE FROM properties WHERE landlord_id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $data->id);
        $stmt->execute();
        
        // Then delete the landlord
        $query = "DELETE FROM landlords WHERE id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $data->id);
        
        if($stmt->execute()) {
            http_response_code(200);
            echo json_encode(["status" => "success", "message" => "Landlord deleted successfully"]);
        } else {
            http_response_code(503);
            echo json_encode(["status" => "error", "message" => "Unable to delete landlord"]);
        }
    } else {
        http_response_code(400);
        echo json_encode(["status" => "error", "message" => "Landlord ID is required"]);
    }
} catch(PDOException $e) {
    http_response_code(503);
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
