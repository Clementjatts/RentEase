<?php
/**
 * Request Model
 * Handles contact requests/notifications for properties
 */
class Request {
    private $db;
    private $table = 'requests';

    public function __construct($database) {
        $this->db = $database;
    }

    /**
     * Get all requests for a landlord with property details
     *
     * @param int $landlordId Landlord ID
     * @return array
     */
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

    /**
     * Create new request
     *
     * @param array $data Request data
     * @return int|false The ID of the new request or false on failure
     */
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

    /**
     * Mark request as read
     *
     * @param int $id Request ID
     * @return bool
     */
    public function markAsRead($id) {
        $query = "UPDATE {$this->table} SET is_read = 1 WHERE id = ?";
        $stmt = $this->db->prepare($query);
        return $stmt->execute([(int)$id]);
    }

    /**
     * Get unread count for landlord
     *
     * @param int $landlordId Landlord ID
     * @return int
     */
    public function getUnreadCount($landlordId) {
        $query = "SELECT COUNT(*) as count FROM {$this->table} 
                  WHERE landlord_id = ? AND is_read = 0";
        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$landlordId]);
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        return (int)$result['count'];
    }
}
