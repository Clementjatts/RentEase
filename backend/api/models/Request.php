<?php
// Request model that handles contact requests and notifications for properties
class Request {
    private $db;
    private $table = 'requests';

    // Initializes the model with database connection
    public function __construct($database) {
        $this->db = $database;
    }

    // Retrieves all requests for a specific landlord with property details
    public function getByLandlordId($landlordId) {
        $query = "SELECT r.id, r.property_id, r.landlord_id, r.requester_name, 
                         r.requester_email, r.requester_phone, r.message, r.is_read, 
                         r.created_at, p.title as property_title, p.address as property_address
                  FROM {$this->table} r 
                  JOIN properties p ON r.property_id = p.id 
                  WHERE r.landlord_id = ? 
                  ORDER BY r.created_at DESC";
        
        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$landlordId]);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    // Creates a new contact request with the provided data
    public function create($data) {
        $query = "INSERT INTO {$this->table} (property_id, landlord_id, requester_name, 
                  requester_email, requester_phone, message) 
                  VALUES (?, ?, ?, ?, ?, ?)";
        
        $stmt = $this->db->prepare($query);
        $result = $stmt->execute([
            (int)$data['property_id'],
            (int)$data['landlord_id'],
            $data['requester_name'],
            $data['requester_email'],
            $data['requester_phone'] ?? null,
            $data['message']
        ]);
        
        if ($result) {
            return $this->db->lastInsertId();
        }

        return false;
    }

    // Marks a specific request as read by the landlord
    public function markAsRead($id) {
        $query = "UPDATE {$this->table} SET is_read = 1 WHERE id = ?";
        $stmt = $this->db->prepare($query);
        return $stmt->execute([(int)$id]);
    }

    // Returns the count of unread requests for a specific landlord
    public function getUnreadCount($landlordId) {
        $query = "SELECT COUNT(*) as count FROM {$this->table} 
                  WHERE landlord_id = ? AND is_read = 0";
        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$landlordId]);
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        return (int)$result['count'];
    }
}
