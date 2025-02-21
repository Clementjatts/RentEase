<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

require_once '../config/Database.php';
require_once '../utils/Auth.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    
    // Require admin authentication
    $auth = new Auth($db);
    $auth->requireAuth();
    
    $landlord_id = isset($_GET['id']) ? $_GET['id'] : null;
    
    if($landlord_id) {
        $query = "SELECT * FROM landlords WHERE id = :id";
        $stmt = $db->prepare($query);
        $stmt->bindParam(':id', $landlord_id);
    } else {
        $query = "SELECT * FROM landlords";
        $stmt = $db->prepare($query);
    }
    
    $stmt->execute();
    $landlords = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if($landlords) {
        http_response_code(200);
        echo json_encode(["status" => "success", "data" => $landlords]);
    } else {
        http_response_code(404);
        echo json_encode(["status" => "error", "message" => "No landlords found"]);
    }
} catch(PDOException $e) {
    http_response_code(503);
    echo json_encode(["status" => "error", "message" => $e->getMessage()]);
}
