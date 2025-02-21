<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

require_once '../config/Database.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    
    $property_id = isset($_GET['id']) ? $_GET['id'] : null;
    $landlord_id = isset($_GET['landlord_id']) ? $_GET['landlord_id'] : null;
    
    if($property_id) {
        $query = "SELECT p.*, l.name as landlord_name, l.contact as landlord_contact 
                 FROM properties p 
                 LEFT JOIN landlords l ON p.landlord_id = l.id 
                 WHERE p.id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $property_id);
    } elseif($landlord_id) {
        $query = "SELECT p.*, l.name as landlord_name, l.contact as landlord_contact 
                 FROM properties p 
                 LEFT JOIN landlords l ON p.landlord_id = l.id 
                 WHERE p.landlord_id = :landlord_id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':landlord_id', $landlord_id);
    } else {
        $query = "SELECT p.*, l.name as landlord_name, l.contact as landlord_contact 
                 FROM properties p 
                 LEFT JOIN landlords l ON p.landlord_id = l.id";
        $stmt = $db->prepare($query);
    }
    
    $stmt->execute();
    $properties = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if($properties) {
        http_response_code(200);
        echo json_encode(["status" => "success", "data" => $properties]);
    } else {
        http_response_code(404);
        echo json_encode(["status" => "error", "message" => "No properties found"]);
    }
} catch(PDOException $e) {
    http_response_code(503);
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
