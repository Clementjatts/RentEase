<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

require_once '../config/Database.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    
    $user_id = isset($_GET['user_id']) ? $_GET['user_id'] : null;
    $property_id = isset($_GET['property_id']) ? $_GET['property_id'] : null;
    $landlord_id = isset($_GET['landlord_id']) ? $_GET['landlord_id'] : null;
    
    $query = "SELECT r.*, p.title as property_title, p.description as property_description, 
              l.name as landlord_name, l.contact as landlord_contact 
              FROM user_requests r
              JOIN properties p ON r.property_id = p.id
              JOIN landlords l ON p.landlord_id = l.id
              WHERE 1=1";
    
    $params = [];
    
    if($user_id) {
        $query .= " AND r.user_id = :user_id";
        $params[':user_id'] = $user_id;
    }
    
    if($property_id) {
        $query .= " AND r.property_id = :property_id";
        $params[':property_id'] = $property_id;
    }
    
    if($landlord_id) {
        $query .= " AND p.landlord_id = :landlord_id";
        $params[':landlord_id'] = $landlord_id;
    }
    
    $query .= " ORDER BY r.created_at DESC";
    
    $stmt = $db->prepare($query);
    $stmt->execute($params);
    $requests = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if($requests) {
        http_response_code(200);
        echo json_encode([
            "status" => "success",
            "data" => $requests
        ]);
    } else {
        http_response_code(404);
        echo json_encode([
            "status" => "error",
            "message" => "No requests found"
        ]);
    }
} catch(PDOException $e) {
    http_response_code(503);
    echo json_encode([
        "status" => "error",
        "message" => $e->getMessage()
    ]);
}
