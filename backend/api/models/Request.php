<?php
/**
 * Request Model
 * 
 * Handles database operations for property viewing requests
 */
class Request {
    private $db;
    private $table = 'requests';
    
    /**
     * Constructor
     * 
     * @param PDO $db Database connection
     */
    public function __construct($db) {
        $this->db = $db;
    }
    
    /**
     * Get all requests
     * 
     * @param array $params Optional filter parameters
     * @return array
     */
    public function getAll($params = []) {
        $query = "SELECT r.id, r.user_id, r.property_id, r.requested_date, r.status, r.message, r.created_at,
                  p.title as property_title, p.description as property_description,
                  u.username as user_username, u.email as user_email,
                  l.name as landlord_name, l.contact as landlord_contact
                  FROM {$this->table} r
                  JOIN properties p ON r.property_id = p.id
                  JOIN users u ON r.user_id = u.id
                  JOIN landlords l ON p.landlord_id = l.id";
        
        // Add filters if provided
        $where_clauses = [];
        $parameters = [];
        
        if (isset($params['user_id'])) {
            $where_clauses[] = "r.user_id = ?";
            $parameters[] = (int)$params['user_id'];
        }
        
        if (isset($params['property_id'])) {
            $where_clauses[] = "r.property_id = ?";
            $parameters[] = (int)$params['property_id'];
        }
        
        if (isset($params['status'])) {
            $where_clauses[] = "r.status = ?";
            $parameters[] = $params['status'];
        }
        
        if (isset($params['date_from'])) {
            $where_clauses[] = "r.requested_date >= ?";
            $parameters[] = $params['date_from'];
        }
        
        if (isset($params['date_to'])) {
            $where_clauses[] = "r.requested_date <= ?";
            $parameters[] = $params['date_to'];
        }
        
        if (!empty($where_clauses)) {
            $query .= " WHERE " . implode(" AND ", $where_clauses);
        }
        
        // Add order by
        $query .= " ORDER BY r.created_at DESC";
        
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
     * Get count of requests based on filters
     * 
     * @param array $params Optional filter parameters
     * @return int
     */
    public function getCount($params = []) {
        $query = "SELECT COUNT(*) as total FROM {$this->table} r";
        
        // Add joins if needed for the filters
        $joins = [];
        if (isset($params['landlord_id'])) {
            $joins[] = "JOIN properties p ON r.property_id = p.id";
        }
        
        if (!empty($joins)) {
            $query .= " " . implode(" ", $joins);
        }
        
        // Add filters if provided
        $where_clauses = [];
        $parameters = [];
        
        if (isset($params['user_id'])) {
            $where_clauses[] = "r.user_id = ?";
            $parameters[] = (int)$params['user_id'];
        }
        
        if (isset($params['property_id'])) {
            $where_clauses[] = "r.property_id = ?";
            $parameters[] = (int)$params['property_id'];
        }
        
        if (isset($params['status'])) {
            $where_clauses[] = "r.status = ?";
            $parameters[] = $params['status'];
        }
        
        if (isset($params['landlord_id'])) {
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
     * Get a single request by ID
     * 
     * @param int $id Request ID
     * @return array|false
     */
    public function getById($id) {
        $query = "SELECT r.id, r.user_id, r.property_id, r.requested_date, r.status, r.message, r.created_at,
                  p.title as property_title, p.description as property_description,
                  u.username as user_username, u.email as user_email,
                  l.name as landlord_name, l.contact as landlord_contact
                  FROM {$this->table} r
                  JOIN properties p ON r.property_id = p.id
                  JOIN users u ON r.user_id = u.id
                  JOIN landlords l ON p.landlord_id = l.id
                  WHERE r.id = ?";
        
        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$id]);
        
        return $stmt->fetch(PDO::FETCH_ASSOC);
    }
    
    /**
     * Get requests by user ID
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
     * Get requests by property ID
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
     * Create a new request
     * 
     * @param array $data Request data
     * @return int|false The ID of the new request or false on failure
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
        
        // Check if the user already has a pending or approved request for this property
        $check_query = "SELECT id FROM {$this->table} 
                        WHERE user_id = ? AND property_id = ? AND (status = 'pending' OR status = 'approved')";
        $check_stmt = $this->db->prepare($check_query);
        $check_stmt->execute([
            (int)$data['user_id'],
            (int)$data['property_id']
        ]);
        
        if ($check_stmt->fetch(PDO::FETCH_ASSOC)) {
            // Request already exists
            return -1;
        }
        
        // Insert the new request
        $query = "INSERT INTO {$this->table} (user_id, property_id, requested_date, message, status) 
                  VALUES (?, ?, ?, ?, ?)";
        $stmt = $this->db->prepare($query);
        $result = $stmt->execute([
            (int)$data['user_id'],
            (int)$data['property_id'],
            $data['requested_date'],
            $data['message'] ?? null,
            $data['status'] ?? 'pending'
        ]);
        
        if ($result) {
            return $this->db->lastInsertId();
        }
        
        return false;
    }
    
    /**
     * Update a request
     * 
     * @param int $id Request ID
     * @param array $data Request data
     * @return bool
     */
    public function update($id, $data) {
        // First get the current request to check if it exists
        $current = $this->getById($id);
        if (!$current) {
            return false;
        }
        
        // Build update query based on provided data
        $query = "UPDATE {$this->table} SET ";
        $set_clauses = [];
        $parameters = [];
        
        // Check each field and add to query if provided
        if (isset($data['requested_date'])) {
            $set_clauses[] = "requested_date = ?";
            $parameters[] = $data['requested_date'];
        }
        
        if (isset($data['status'])) {
            $set_clauses[] = "status = ?";
            $parameters[] = $data['status'];
        }
        
        if (isset($data['message'])) {
            $set_clauses[] = "message = ?";
            $parameters[] = $data['message'];
        }
        
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
     * Delete a request
     * 
     * @param int $id Request ID
     * @param int $user_id User ID for authorization (optional)
     * @return bool
     */
    public function delete($id, $user_id = null) {
        $query = "DELETE FROM {$this->table} WHERE id = ?";
        $params = [(int)$id];
        
        // If user_id is provided, ensure the request belongs to this user
        if ($user_id !== null) {
            $query .= " AND user_id = ?";
            $params[] = (int)$user_id;
        }
        
        $stmt = $this->db->prepare($query);
        return $stmt->execute($params);
    }
}
