<?php
class Auth {
    private $db;
    
    public function __construct($db) {
        $this->db = $db;
    }
    
    public function authenticateAdmin($username, $password) {
        $query = "SELECT id FROM admins WHERE username = :username AND password = :password";
        $stmt = $this->db->prepare($query);
        $stmt->bindParam(':username', $username);
        $stmt->bindParam(':password', $password);
        $stmt->execute();
        
        if($stmt->rowCount() > 0) {
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            return $result['id'];
        }
        return false;
    }
    
    public function authenticateLandlord($id) {
        $query = "SELECT id FROM landlords WHERE id = :id";
        $stmt = $this->db->prepare($query);
        $stmt->bindParam(':id', $id);
        $stmt->execute();
        
        return $stmt->rowCount() > 0;
    }
    
    public function isPropertyOwner($landlord_id, $property_id) {
        $query = "SELECT id FROM properties WHERE id = :property_id AND landlord_id = :landlord_id";
        $stmt = $this->db->prepare($query);
        $stmt->bindParam(':property_id', $property_id);
        $stmt->bindParam(':landlord_id', $landlord_id);
        $stmt->execute();
        
        return $stmt->rowCount() > 0;
    }
    
    public function requireAuth() {
        if (!isset($_SERVER['PHP_AUTH_USER']) || !isset($_SERVER['PHP_AUTH_PW'])) {
            $this->sendAuthenticationPrompt();
            exit;
        }
        
        $admin_id = $this->authenticateAdmin($_SERVER['PHP_AUTH_USER'], $_SERVER['PHP_AUTH_PW']);
        if (!$admin_id) {
            $this->sendAuthenticationPrompt();
            exit;
        }
        
        return $admin_id;
    }
    
    public function requireLandlordAuth($landlord_id) {
        // First require admin authentication
        $admin_id = $this->requireAuth();
        
        // Then verify the landlord exists
        if (!$this->authenticateLandlord($landlord_id)) {
            http_response_code(404);
            echo json_encode(["status" => "error", "message" => "Landlord not found"]);
            exit;
        }
        
        return $admin_id;
    }
    
    private function sendAuthenticationPrompt() {
        header('WWW-Authenticate: Basic realm="RentEase Admin Access"');
        http_response_code(401);
        echo json_encode(["status" => "error", "message" => "Authentication required"]);
    }
}
