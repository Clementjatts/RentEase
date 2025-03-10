<?php
/**
 * User Model
 * 
 * Handles database operations for users
 */
class User {
    private $db;
    private $table = 'users';
    
    /**
     * Constructor
     * 
     * @param PDO $db Database connection
     */
    public function __construct($db) {
        $this->db = $db;
    }
    
    /**
     * Get all users
     * 
     * @param array $params Optional filter parameters
     * @return array
     */
    public function getAll($params = []) {
        $query = "SELECT id, username, email, role, created_at, updated_at FROM {$this->table}";
        
        // Add filters if provided
        $where_clauses = [];
        $parameters = [];
        
        if (isset($params['role'])) {
            $where_clauses[] = "role = ?";
            $parameters[] = $params['role'];
        }
        
        if (!empty($where_clauses)) {
            $query .= " WHERE " . implode(" AND ", $where_clauses);
        }
        
        // Add order by
        $query .= " ORDER BY created_at DESC";
        
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
     * Get a single user by ID
     * 
     * @param int $id User ID
     * @return array|false
     */
    public function getById($id) {
        $query = "SELECT id, username, email, role, created_at, updated_at 
                  FROM {$this->table} WHERE id = ?";
        
        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$id]);
        
        return $stmt->fetch(PDO::FETCH_ASSOC);
    }
    
    /**
     * Get a user by username
     * 
     * @param string $username Username
     * @return array|false
     */
    public function getByUsername($username) {
        $query = "SELECT id, username, email, password, role, created_at, updated_at 
                  FROM {$this->table} WHERE username = ?";
        
        $stmt = $this->db->prepare($query);
        $stmt->execute([$username]);
        
        return $stmt->fetch(PDO::FETCH_ASSOC);
    }
    
    /**
     * Get a user by email
     * 
     * @param string $email Email
     * @return array|false
     */
    public function getByEmail($email) {
        $query = "SELECT id, username, email, password, role, created_at, updated_at 
                  FROM {$this->table} WHERE email = ?";
        
        $stmt = $this->db->prepare($query);
        $stmt->execute([$email]);
        
        return $stmt->fetch(PDO::FETCH_ASSOC);
    }
    
    /**
     * Create a new user
     * 
     * @param array $data User data
     * @return int|false The ID of the new user or false on failure
     */
    public function create($data) {
        // Check if username or email already exists
        $check_query = "SELECT id FROM {$this->table} WHERE username = ? OR email = ?";
        $check_stmt = $this->db->prepare($check_query);
        $check_stmt->execute([$data['username'], $data['email']]);
        
        if ($check_stmt->fetch(PDO::FETCH_ASSOC)) {
            // Username or email already exists
            return -1;
        }
        
        // Hash the password
        $password_hash = password_hash($data['password'], PASSWORD_DEFAULT);
        
        // Insert the new user
        $query = "INSERT INTO {$this->table} (username, email, password, role) 
                  VALUES (?, ?, ?, ?)";
        $stmt = $this->db->prepare($query);
        $result = $stmt->execute([
            $data['username'],
            $data['email'],
            $password_hash,
            $data['role'] ?? 'user'
        ]);
        
        if ($result) {
            return $this->db->lastInsertId();
        }
        
        return false;
    }
    
    /**
     * Update a user
     * 
     * @param int $id User ID
     * @param array $data User data
     * @return bool
     */
    public function update($id, $data) {
        // First get the current user to check if it exists
        $current = $this->getById($id);
        if (!$current) {
            return false;
        }
        
        // Check if username or email already exists for another user
        if (isset($data['username']) || isset($data['email'])) {
            $check_query = "SELECT id FROM {$this->table} WHERE (";
            $check_params = [];
            
            if (isset($data['username'])) {
                $check_query .= "username = ?";
                $check_params[] = $data['username'];
            }
            
            if (isset($data['email'])) {
                if (isset($data['username'])) {
                    $check_query .= " OR ";
                }
                $check_query .= "email = ?";
                $check_params[] = $data['email'];
            }
            
            $check_query .= ") AND id != ?";
            $check_params[] = (int)$id;
            
            $check_stmt = $this->db->prepare($check_query);
            $check_stmt->execute($check_params);
            
            if ($check_stmt->fetch(PDO::FETCH_ASSOC)) {
                // Username or email already exists for another user
                return false;
            }
        }
        
        // Build update query based on provided data
        $query = "UPDATE {$this->table} SET ";
        $set_clauses = [];
        $parameters = [];
        
        // Check each field and add to query if provided
        if (isset($data['username'])) {
            $set_clauses[] = "username = ?";
            $parameters[] = $data['username'];
        }
        
        if (isset($data['email'])) {
            $set_clauses[] = "email = ?";
            $parameters[] = $data['email'];
        }
        
        if (isset($data['password'])) {
            $set_clauses[] = "password = ?";
            $parameters[] = password_hash($data['password'], PASSWORD_DEFAULT);
        }
        
        if (isset($data['role'])) {
            $set_clauses[] = "role = ?";
            $parameters[] = $data['role'];
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
     * Delete a user
     * 
     * @param int $id User ID
     * @return bool
     */
    public function delete($id) {
        // Check if the user exists
        $current = $this->getById($id);
        if (!$current) {
            return false;
        }
        
        // Check if there are any dependent records (favorites, requests)
        $check_favorites = "SELECT COUNT(*) as count FROM favorites WHERE user_id = ?";
        $favorites_stmt = $this->db->prepare($check_favorites);
        $favorites_stmt->execute([(int)$id]);
        $favorites_count = $favorites_stmt->fetch(PDO::FETCH_ASSOC)['count'];
        
        $check_requests = "SELECT COUNT(*) as count FROM requests WHERE user_id = ?";
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
    
    /**
     * Verify a user's credentials
     * 
     * @param string $username Username or email
     * @param string $password Password
     * @return array|false User data if credentials are valid, false otherwise
     */
    public function verify($username, $password) {
        // Check if input is email or username
        $is_email = filter_var($username, FILTER_VALIDATE_EMAIL);
        
        if ($is_email) {
            $user = $this->getByEmail($username);
        } else {
            $user = $this->getByUsername($username);
        }
        
        if (!$user) {
            return false;
        }
        
        // Verify password
        if (password_verify($password, $user['password'])) {
            // Remove password from returned data
            unset($user['password']);
            return $user;
        }
        
        return false;
    }
}
