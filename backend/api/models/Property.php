<?php
/**
 * Property Model
 * 
 * Handles database operations for properties
 */
class Property {
    private $db;
    private $table = 'properties';
    
    /**
     * Constructor
     * 
     * @param PDO $db Database connection
     */
    public function __construct($db) {
        $this->db = $db;
    }
    
    /**
     * Get all properties
     * 
     * @param array $params Optional filter parameters and pagination
     * @return array
     */
    public function getAll($params = []) {
        // Base query
        $query = "SELECT p.id, p.title, p.description, p.price, p.bedroom_count, p.bathroom_count, 
                  p.size, p.status, p.furniture_type, p.image_url, p.created_at, p.updated_at,
                  l.id as landlord_id, l.name as landlord_name, l.contact as landlord_contact
                  FROM {$this->table} p
                  JOIN landlords l ON p.landlord_id = l.id";
        
        // Add filters if provided
        $where_clauses = [];
        $parameters = [];
        
        if (isset($params['status']) && !empty($params['status'])) {
            $where_clauses[] = "p.status = ?";
            $parameters[] = $params['status'];
        }
        
        if (isset($params['min_price']) && !empty($params['min_price'])) {
            $where_clauses[] = "p.price >= ?";
            $parameters[] = (float)$params['min_price'];
        }
        
        if (isset($params['max_price']) && !empty($params['max_price'])) {
            $where_clauses[] = "p.price <= ?";
            $parameters[] = (float)$params['max_price'];
        }
        
        if (isset($params['bedroom_count']) && !empty($params['bedroom_count'])) {
            $where_clauses[] = "p.bedroom_count >= ?";
            $parameters[] = (int)$params['bedroom_count'];
        }
        
        if (isset($params['bathroom_count']) && !empty($params['bathroom_count'])) {
            $where_clauses[] = "p.bathroom_count >= ?";
            $parameters[] = (int)$params['bathroom_count'];
        }
        
        if (isset($params['furniture_type']) && !empty($params['furniture_type'])) {
            $where_clauses[] = "p.furniture_type = ?";
            $parameters[] = $params['furniture_type'];
        }
        
        if (isset($params['landlord_id']) && !empty($params['landlord_id'])) {
            $where_clauses[] = "p.landlord_id = ?";
            $parameters[] = (int)$params['landlord_id'];
        }
        
        if (!empty($where_clauses)) {
            $query .= " WHERE " . implode(" AND ", $where_clauses);
        }
        
        // Add order by
        $query .= " ORDER BY p.updated_at DESC";
        
        // Add pagination
        $page = isset($params['page']) ? (int)$params['page'] : 1;
        $limit = isset($params['limit']) ? (int)$params['limit'] : 20;
        $offset = ($page - 1) * $limit;
        
        $query .= " LIMIT ? OFFSET ?";
        $parameters[] = $limit;
        $parameters[] = $offset;
        
        // Prepare and execute the query
        $stmt = $this->db->prepare($query);
        $stmt->execute($parameters);
        
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
    
    /**
     * Get total count of properties based on filters
     * 
     * @param array $params Optional filter parameters
     * @return int
     */
    public function getCount($params = []) {
        // Base query
        $query = "SELECT COUNT(*) as total FROM {$this->table} p";
        
        // Add filters if provided
        $where_clauses = [];
        $parameters = [];
        
        if (isset($params['status']) && !empty($params['status'])) {
            $where_clauses[] = "p.status = ?";
            $parameters[] = $params['status'];
        }
        
        if (isset($params['min_price']) && !empty($params['min_price'])) {
            $where_clauses[] = "p.price >= ?";
            $parameters[] = (float)$params['min_price'];
        }
        
        if (isset($params['max_price']) && !empty($params['max_price'])) {
            $where_clauses[] = "p.price <= ?";
            $parameters[] = (float)$params['max_price'];
        }
        
        if (isset($params['bedroom_count']) && !empty($params['bedroom_count'])) {
            $where_clauses[] = "p.bedroom_count >= ?";
            $parameters[] = (int)$params['bedroom_count'];
        }
        
        if (isset($params['bathroom_count']) && !empty($params['bathroom_count'])) {
            $where_clauses[] = "p.bathroom_count >= ?";
            $parameters[] = (int)$params['bathroom_count'];
        }
        
        if (isset($params['furniture_type']) && !empty($params['furniture_type'])) {
            $where_clauses[] = "p.furniture_type = ?";
            $parameters[] = $params['furniture_type'];
        }
        
        if (isset($params['landlord_id']) && !empty($params['landlord_id'])) {
            $where_clauses[] = "p.landlord_id = ?";
            $parameters[] = (int)$params['landlord_id'];
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
     * Get a single property by ID
     * 
     * @param int $id Property ID
     * @return array|false
     */
    public function getById($id) {
        $query = "SELECT p.id, p.title, p.description, p.price, p.bedroom_count, p.bathroom_count, 
                  p.size, p.status, p.furniture_type, p.image_url, p.created_at, p.updated_at,
                  l.id as landlord_id, l.name as landlord_name, l.contact as landlord_contact
                  FROM {$this->table} p
                  JOIN landlords l ON p.landlord_id = l.id
                  WHERE p.id = ?";
        
        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$id]);
        
        return $stmt->fetch(PDO::FETCH_ASSOC);
    }
    
    /**
     * Search properties
     * 
     * @param string $search_term Search term
     * @param array $params Optional filter parameters and pagination
     * @return array
     */
    public function search($search_term, $params = []) {
        // Base query
        $search_term = '%' . $search_term . '%';
        
        $query = "SELECT p.id, p.title, p.description, p.price, p.bedroom_count, p.bathroom_count, 
                  p.size, p.status, p.furniture_type, p.image_url, p.created_at, p.updated_at,
                  l.id as landlord_id, l.name as landlord_name, l.contact as landlord_contact
                  FROM {$this->table} p
                  JOIN landlords l ON p.landlord_id = l.id
                  WHERE (p.title LIKE ? OR p.description LIKE ? OR l.name LIKE ?)";
        
        $parameters = [$search_term, $search_term, $search_term];
        
        // Add filters if provided
        if (isset($params['status']) && !empty($params['status'])) {
            $query .= " AND p.status = ?";
            $parameters[] = $params['status'];
        }
        
        if (isset($params['min_price']) && !empty($params['min_price'])) {
            $query .= " AND p.price >= ?";
            $parameters[] = (float)$params['min_price'];
        }
        
        if (isset($params['max_price']) && !empty($params['max_price'])) {
            $query .= " AND p.price <= ?";
            $parameters[] = (float)$params['max_price'];
        }
        
        // Add order by
        $query .= " ORDER BY p.updated_at DESC";
        
        // Add pagination
        $page = isset($params['page']) ? (int)$params['page'] : 1;
        $limit = isset($params['limit']) ? (int)$params['limit'] : 20;
        $offset = ($page - 1) * $limit;
        
        $query .= " LIMIT ? OFFSET ?";
        $parameters[] = $limit;
        $parameters[] = $offset;
        
        // Prepare and execute the query
        $stmt = $this->db->prepare($query);
        $stmt->execute($parameters);
        
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
    
    /**
     * Create a new property
     * 
     * @param array $data Property data
     * @return int|false The ID of the new property or false on failure
     */
    public function create($data) {
        // Validate landlord exists
        $landlord_check = "SELECT id FROM landlords WHERE id = ?";
        $landlord_stmt = $this->db->prepare($landlord_check);
        $landlord_stmt->execute([(int)$data['landlord_id']]);
        $landlord_exists = $landlord_stmt->fetch(PDO::FETCH_ASSOC);
        
        if (!$landlord_exists) {
            return false;
        }
        
        $query = "INSERT INTO {$this->table} (
                    title, description, price, bedroom_count, bathroom_count, 
                    size, status, furniture_type, image_url, landlord_id
                  ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        $stmt = $this->db->prepare($query);
        $result = $stmt->execute([
            $data['title'],
            $data['description'],
            (float)$data['price'],
            (int)$data['bedroom_count'],
            (int)$data['bathroom_count'],
            (float)$data['size'],
            $data['status'],
            $data['furniture_type'],
            $data['image_url'] ?? null,
            (int)$data['landlord_id']
        ]);
        
        if ($result) {
            return $this->db->lastInsertId();
        }
        
        return false;
    }
    
    /**
     * Update a property
     * 
     * @param int $id Property ID
     * @param array $data Property data
     * @return bool
     */
    public function update($id, $data) {
        // First get the current property to check if it exists
        $current = $this->getById($id);
        if (!$current) {
            return false;
        }
        
        // Build update query based on provided data
        $query = "UPDATE {$this->table} SET ";
        $set_clauses = [];
        $parameters = [];
        
        // Check each field and add to query if provided
        if (isset($data['title'])) {
            $set_clauses[] = "title = ?";
            $parameters[] = $data['title'];
        }
        
        if (isset($data['description'])) {
            $set_clauses[] = "description = ?";
            $parameters[] = $data['description'];
        }
        
        if (isset($data['price'])) {
            $set_clauses[] = "price = ?";
            $parameters[] = (float)$data['price'];
        }
        
        if (isset($data['bedroom_count'])) {
            $set_clauses[] = "bedroom_count = ?";
            $parameters[] = (int)$data['bedroom_count'];
        }
        
        if (isset($data['bathroom_count'])) {
            $set_clauses[] = "bathroom_count = ?";
            $parameters[] = (int)$data['bathroom_count'];
        }
        
        if (isset($data['size'])) {
            $set_clauses[] = "size = ?";
            $parameters[] = (float)$data['size'];
        }
        
        if (isset($data['status'])) {
            $set_clauses[] = "status = ?";
            $parameters[] = $data['status'];
        }
        
        if (isset($data['furniture_type'])) {
            $set_clauses[] = "furniture_type = ?";
            $parameters[] = $data['furniture_type'];
        }
        
        if (isset($data['image_url'])) {
            $set_clauses[] = "image_url = ?";
            $parameters[] = $data['image_url'];
        }
        
        if (isset($data['landlord_id'])) {
            // Validate landlord exists
            $landlord_check = "SELECT id FROM landlords WHERE id = ?";
            $landlord_stmt = $this->db->prepare($landlord_check);
            $landlord_stmt->execute([(int)$data['landlord_id']]);
            $landlord_exists = $landlord_stmt->fetch(PDO::FETCH_ASSOC);
            
            if (!$landlord_exists) {
                return false;
            }
            
            $set_clauses[] = "landlord_id = ?";
            $parameters[] = (int)$data['landlord_id'];
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
     * Delete a property
     * 
     * @param int $id Property ID
     * @return bool
     */
    public function delete($id) {
        // Check if the property exists
        $current = $this->getById($id);
        if (!$current) {
            return false;
        }
        
        // Check if there are any dependent records (favorites, requests)
        $check_favorites = "SELECT COUNT(*) as count FROM favorites WHERE property_id = ?";
        $favorites_stmt = $this->db->prepare($check_favorites);
        $favorites_stmt->execute([(int)$id]);
        $favorites_count = $favorites_stmt->fetch(PDO::FETCH_ASSOC)['count'];
        
        $check_requests = "SELECT COUNT(*) as count FROM requests WHERE property_id = ?";
        $requests_stmt = $this->db->prepare($check_requests);
        $requests_stmt->execute([(int)$id]);
        $requests_count = $requests_stmt->fetch(PDO::FETCH_ASSOC)['count'];
        
        // If there are dependent records, don't delete
        if ($favorites_count > 0 || $requests_count > 0) {
            return false;
        }
        
        $query = "DELETE FROM {$this->table} WHERE id = ?";
        $stmt = $this->db->prepare($query);
        return $stmt->execute([(int)$id]);
    }
}
