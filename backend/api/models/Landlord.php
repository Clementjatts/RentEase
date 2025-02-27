<?php
/**
 * Landlord Model
 * 
 * Handles database operations for landlords
 */
class Landlord {
    private $db;
    private $table = 'landlords';
    
    /**
     * Constructor
     * 
     * @param PDO $db Database connection
     */
    public function __construct($db) {
        $this->db = $db;
    }
    
    /**
     * Get all landlords
     * 
     * @param array $params Optional filter parameters
     * @return array
     */
    public function getAll($params = []) {
        $query = "SELECT l.id, l.name, l.contact, l.email, l.created_at, l.updated_at,
                 (SELECT COUNT(*) FROM properties WHERE landlord_id = l.id) as property_count
                 FROM {$this->table} l";
        
        // Add filters if provided
        $where_clauses = [];
        $parameters = [];
        
        if (isset($params['name'])) {
            $where_clauses[] = "l.name LIKE ?";
            $parameters[] = '%' . $params['name'] . '%';
        }
        
        if (!empty($where_clauses)) {
            $query .= " WHERE " . implode(" AND ", $where_clauses);
        }
        
        // Add order by
        $query .= " ORDER BY l.name ASC";
        
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
     * Get count of landlords
     * 
     * @param array $params Optional filter parameters
     * @return int
     */
    public function getCount($params = []) {
        $query = "SELECT COUNT(*) as total FROM {$this->table} l";
        
        // Add filters if provided
        $where_clauses = [];
        $parameters = [];
        
        if (isset($params['name'])) {
            $where_clauses[] = "l.name LIKE ?";
            $parameters[] = '%' . $params['name'] . '%';
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
     * Get a single landlord by ID
     * 
     * @param int $id Landlord ID
     * @return array|false
     */
    public function getById($id) {
        $query = "SELECT l.id, l.name, l.contact, l.email, l.created_at, l.updated_at,
                 (SELECT COUNT(*) FROM properties WHERE landlord_id = l.id) as property_count
                 FROM {$this->table} l
                 WHERE l.id = ?";
        
        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$id]);
        
        $landlord = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($landlord) {
            // Get properties for this landlord
            $properties_query = "SELECT id, title, description, price, bedroom_count, bathroom_count,
                               size, status, furniture_type, image_url
                               FROM properties WHERE landlord_id = ?
                               ORDER BY created_at DESC";
            $properties_stmt = $this->db->prepare($properties_query);
            $properties_stmt->execute([(int)$id]);
            $landlord['properties'] = $properties_stmt->fetchAll(PDO::FETCH_ASSOC);
        }
        
        return $landlord;
    }
    
    /**
     * Create a new landlord
     * 
     * @param array $data Landlord data
     * @return int|false The ID of the new landlord or false on failure
     */
    public function create($data) {
        // Check if email already exists
        if (isset($data['email']) && !empty($data['email'])) {
            $check_query = "SELECT id FROM {$this->table} WHERE email = ?";
            $check_stmt = $this->db->prepare($check_query);
            $check_stmt->execute([$data['email']]);
            
            if ($check_stmt->fetch(PDO::FETCH_ASSOC)) {
                // Email already exists
                return -1;
            }
        }
        
        // Insert the new landlord
        $query = "INSERT INTO {$this->table} (name, contact, email) VALUES (?, ?, ?)";
        $stmt = $this->db->prepare($query);
        $result = $stmt->execute([
            $data['name'],
            $data['contact'],
            $data['email'] ?? null
        ]);
        
        if ($result) {
            return $this->db->lastInsertId();
        }
        
        return false;
    }
    
    /**
     * Update a landlord
     * 
     * @param int $id Landlord ID
     * @param array $data Landlord data
     * @return bool
     */
    public function update($id, $data) {
        // First get the current landlord to check if it exists
        $current = $this->getById($id);
        if (!$current) {
            return false;
        }
        
        // Check if email already exists for another landlord
        if (isset($data['email']) && !empty($data['email'])) {
            $check_query = "SELECT id FROM {$this->table} WHERE email = ? AND id != ?";
            $check_stmt = $this->db->prepare($check_query);
            $check_stmt->execute([$data['email'], (int)$id]);
            
            if ($check_stmt->fetch(PDO::FETCH_ASSOC)) {
                // Email already exists for another landlord
                return false;
            }
        }
        
        // Build update query based on provided data
        $query = "UPDATE {$this->table} SET ";
        $set_clauses = [];
        $parameters = [];
        
        // Check each field and add to query if provided
        if (isset($data['name'])) {
            $set_clauses[] = "name = ?";
            $parameters[] = $data['name'];
        }
        
        if (isset($data['contact'])) {
            $set_clauses[] = "contact = ?";
            $parameters[] = $data['contact'];
        }
        
        if (isset($data['email'])) {
            $set_clauses[] = "email = ?";
            $parameters[] = $data['email'];
        }
        
        // Add updated_at timestamp
        $set_clauses[] = "updated_at = CURRENT_TIMESTAMP";
        
        // If nothing to update, return true
        if (empty($set_clauses)) {
            return true;
        }
        
        // Complete the query
        $query .= implode(", ", $set_clauses);
        $query .= " WHERE id = ?";
        $parameters[] = (int)$id;
        
        // Execute the query
        $stmt = $this->db->prepare($query);
        return $stmt->execute($parameters);
    }
    
    /**
     * Delete a landlord
     * 
     * @param int $id Landlord ID
     * @return bool
     */
    public function delete($id) {
        // Check if the landlord exists
        $current = $this->getById($id);
        if (!$current) {
            return false;
        }
        
        // Check if there are any properties for this landlord
        if ($current['property_count'] > 0) {
            return false;
        }
        
        $query = "DELETE FROM {$this->table} WHERE id = ?";
        $stmt = $this->db->prepare($query);
        return $stmt->execute([(int)$id]);
    }
}
