<?php
/**
 * Favorite Model
 * 
 * Handles database operations for favorites
 */
class Favorite {
    private $db;
    private $table = 'favorites';
    
    /**
     * Constructor
     * 
     * @param PDO $db Database connection
     */
    public function __construct($db) {
        $this->db = $db;
    }
    
    /**
     * Get all favorites
     * 
     * @param array $params Optional filter parameters
     * @return array
     */
    public function getAll($params = []) {
        $query = "SELECT f.id, f.user_id, f.property_id, f.created_at,
                  p.title as property_title, p.description as property_description,
                  p.price as property_price, p.image_url as property_image,
                  l.name as landlord_name, l.contact as landlord_contact
                  FROM {$this->table} f
                  JOIN properties p ON f.property_id = p.id
                  JOIN landlords l ON p.landlord_id = l.id";
        
        // Add filters if provided
        $where_clauses = [];
        $parameters = [];
        
        if (isset($params['user_id'])) {
            $where_clauses[] = "f.user_id = ?";
            $parameters[] = (int)$params['user_id'];
        }
        
        if (isset($params['property_id'])) {
            $where_clauses[] = "f.property_id = ?";
            $parameters[] = (int)$params['property_id'];
        }
        
        if (!empty($where_clauses)) {
            $query .= " WHERE " . implode(" AND ", $where_clauses);
        }
        
        // Add order by
        $query .= " ORDER BY f.created_at DESC";
        
        // Add pagination
        if (isset($params['page']) && isset($params['limit'])) {
            $page = (int)$params['page'];
            $limit = (int)$params['limit'];
            $offset = ($page - 1) * $limit;
            
            $query .= " LIMIT ? OFFSET ?";
            $parameters[] = $limit;
            $parameters[] = $offset;
        }
        
        // Prepare and execute the query
        $stmt = $this->db->prepare($query);
        $stmt->execute($parameters);
        
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
    
    /**
     * Get count of favorites based on filters
     * 
     * @param array $params Optional filter parameters
     * @return int
     */
    public function getCount($params = []) {
        $query = "SELECT COUNT(*) as total FROM {$this->table} f";
        
        // Add filters if provided
        $where_clauses = [];
        $parameters = [];
        
        if (isset($params['user_id'])) {
            $where_clauses[] = "f.user_id = ?";
            $parameters[] = (int)$params['user_id'];
        }
        
        if (isset($params['property_id'])) {
            $where_clauses[] = "f.property_id = ?";
            $parameters[] = (int)$params['property_id'];
        }
        
        if (!empty($where_clauses)) {
            $query .= " WHERE " . implode(" AND ", $where_clauses);
        }
        
        // Prepare and execute the query
        $stmt = $this->db->prepare($query);
        $stmt->execute($parameters);
        
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        return (int)$result['total'];
    }
    
    /**
     * Get a single favorite by ID
     * 
     * @param int $id Favorite ID
     * @return array|false
     */
    public function getById($id) {
        $query = "SELECT f.id, f.user_id, f.property_id, f.created_at,
                  p.title as property_title, p.description as property_description,
                  p.price as property_price, p.image_url as property_image,
                  l.name as landlord_name, l.contact as landlord_contact
                  FROM {$this->table} f
                  JOIN properties p ON f.property_id = p.id
                  JOIN landlords l ON p.landlord_id = l.id
                  WHERE f.id = ?";
        
        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$id]);
        
        return $stmt->fetch(PDO::FETCH_ASSOC);
    }
    
    /**
     * Get favorites by user ID
     * 
     * @param int $user_id User ID
     * @param array $params Optional pagination parameters
     * @return array
     */
    public function getByUser($user_id, $params = []) {
        $params['user_id'] = $user_id;
        return $this->getAll($params);
    }
    
    /**
     * Get favorites by property ID
     * 
     * @param int $property_id Property ID
     * @param array $params Optional pagination parameters
     * @return array
     */
    public function getByProperty($property_id, $params = []) {
        $params['property_id'] = $property_id;
        return $this->getAll($params);
    }
    
    /**
     * Create a new favorite
     * 
     * @param array $data Favorite data
     * @return int|false The ID of the new favorite or false on failure
     */
    public function create($data) {
        // First check if the property exists
        $property_check = "SELECT id FROM properties WHERE id = ?";
        $property_stmt = $this->db->prepare($property_check);
        $property_stmt->execute([(int)$data['property_id']]);
        $property_exists = $property_stmt->fetch(PDO::FETCH_ASSOC);
        
        if (!$property_exists) {
            return false;
        }
        
        // Check if this favorite already exists
        $check_query = "SELECT id FROM {$this->table} 
                        WHERE user_id = ? AND property_id = ?";
        $check_stmt = $this->db->prepare($check_query);
        $check_stmt->execute([
            (int)$data['user_id'],
            (int)$data['property_id']
        ]);
        
        if ($check_stmt->fetch(PDO::FETCH_ASSOC)) {
            // Favorite already exists
            return -1;
        }
        
        // Insert the new favorite
        $query = "INSERT INTO {$this->table} (user_id, property_id) VALUES (?, ?)";
        $stmt = $this->db->prepare($query);
        $result = $stmt->execute([
            (int)$data['user_id'],
            (int)$data['property_id']
        ]);
        
        if ($result) {
            return $this->db->lastInsertId();
        }
        
        return false;
    }
    
    /**
     * Delete a favorite
     * 
     * @param int $id Favorite ID
     * @param int $user_id User ID for authorization (optional)
     * @return bool
     */
    public function delete($id, $user_id = null) {
        $query = "DELETE FROM {$this->table} WHERE id = ?";
        $params = [(int)$id];
        
        // If user_id is provided, ensure the favorite belongs to this user
        if ($user_id !== null) {
            $query .= " AND user_id = ?";
            $params[] = (int)$user_id;
        }
        
        $stmt = $this->db->prepare($query);
        return $stmt->execute($params);
    }
    
    /**
     * Check if a favorite exists
     * 
     * @param int $user_id User ID
     * @param int $property_id Property ID
     * @return bool
     */
    public function exists($user_id, $property_id) {
        $query = "SELECT id FROM {$this->table} 
                  WHERE user_id = ? AND property_id = ?";
        $stmt = $this->db->prepare($query);
        $stmt->execute([
            (int)$user_id,
            (int)$property_id
        ]);
        
        return $stmt->fetch(PDO::FETCH_ASSOC) ? true : false;
    }
}
