<?php
// User model that handles database operations for users including authentication
class User {
    private $db;
    private $table = 'users';

    // Initializes the model with database connection
    public function __construct($db) {
        $this->db = $db;
    }

    // Retrieves all users with optional filtering and pagination
    public function getAll($params = []) {
        // Include all user fields for simplified schema
        $query = "SELECT id, username, email, phone, user_type, full_name, created_at FROM {$this->table}";

        // Add filters if provided
        $where_clauses = [];
        $parameters = [];

        if (isset($params['user_type'])) {
            $where_clauses[] = "user_type = ?";
            $parameters[] = $params['user_type'];
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

    // Retrieves a single user by ID without password field
    public function getById($id) {
        // Include all fields needed by the Android app
        $query = "SELECT id, username, email, user_type, full_name, phone, created_at
                  FROM {$this->table} WHERE id = ?";

        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$id]);

        return $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // Retrieves a single user by ID including password for authentication purposes
    public function getByIdWithPassword($id) {
        $query = "SELECT id, username, email, password, user_type, full_name, phone, created_at
                  FROM {$this->table} WHERE id = ?";

        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$id]);

        return $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // Retrieves a user by username including password for authentication
    public function getByUsername($username) {
        // Removed updated_at from the query
        $query = "SELECT id, username, email, password, user_type, created_at
                  FROM {$this->table} WHERE username = ?";

        $stmt = $this->db->prepare($query);
        $stmt->execute([$username]);

        return $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // Retrieves a user by email including password for authentication
    public function getByEmail($email) {
        // Removed updated_at from the query
        $query = "SELECT id, username, email, password, user_type, created_at
                  FROM {$this->table} WHERE email = ?";

        $stmt = $this->db->prepare($query);
        $stmt->execute([$email]);

        return $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // Creates a new user with password hashing and duplicate checking
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
        $query = "INSERT INTO {$this->table} (username, email, password, user_type, full_name, phone)
                  VALUES (?, ?, ?, ?, ?, ?)";
        $stmt = $this->db->prepare($query);
        $result = $stmt->execute([
            $data['username'],
            $data['email'],
            $password_hash,
            $data['user_type'] ?? 'user',
            $data['full_name'] ?? null,
            $data['phone'] ?? null
        ]);

        if ($result) {
            return $this->db->lastInsertId();
        }

        return false;
    }

    // Updates an existing user with duplicate checking and password hashing
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

        if (isset($data['user_type'])) {
            $set_clauses[] = "user_type = ?";
            $parameters[] = $data['user_type'];
        }

        if (isset($data['full_name'])) {
            $set_clauses[] = "full_name = ?";
            $parameters[] = $data['full_name'];
        }

        if (isset($data['phone'])) {
            $set_clauses[] = "phone = ?";
            $parameters[] = $data['phone'];
        }

        // Removed updated_at timestamp since the column doesn't exist

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
        $result = $stmt->execute($parameters);

        // No landlord synchronization needed with simplified schema

        return $result;
    }



    // Deletes a user by ID after existence check
    public function delete($id) {
        // Check if the user exists
        $current = $this->getById($id);
        if (!$current) {
            return false;
        }

        $query = "DELETE FROM {$this->table} WHERE id = ?";
        $stmt = $this->db->prepare($query);
        return $stmt->execute([(int)$id]);
    }

    // Verifies user credentials using secure password hashing
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

        // Verify password using secure hash verification
        if (password_verify($password, $user['password'])) {
            // Remove password from returned data
            unset($user['password']);
            return $user;
        }

        return false;
    }
}
