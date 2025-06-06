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
        // Simplified query - direct JOIN with users table
        $query = "SELECT p.id, p.title, p.description, p.price, p.bedroom_count, p.bathroom_count,
                  p.furniture_type, p.address, p.created_at, p.updated_at, p.image_url,
                  p.landlord_id, u.full_name as landlord_name,
                  (u.email || ',' || u.phone) as landlord_contact
                  FROM {$this->table} p
                  JOIN users u ON p.landlord_id = u.id
                  WHERE u.user_type = 'LANDLORD'";

        // Add filters if provided
        $where_clauses = [];
        $parameters = [];



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
            $query .= " AND " . implode(" AND ", $where_clauses);
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

        $properties = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // Process image URLs for each property (now stored directly in properties table)
        foreach ($properties as &$property) {
            // Image URL is now directly stored in the properties table
            if ($property['image_url']) {
                $base_url = Config::get('base_url');
                // Extract filename from any existing URL format
                $filename = basename($property['image_url']);
                // Construct clean URL using current base URL
                $property['image_url'] = $base_url . '/uploads/properties/' . $filename;
            }

            // Extract just the city from the full address
            if (!empty($property['address'])) {
                $address_parts = explode(',', $property['address']);
                // Try to get the city - usually the last or second-to-last part
                if (count($address_parts) > 1) {
                    // Clean up whitespace and get the last part that looks like a city
                    $city_part = trim(end($address_parts));
                    // If the last part has postal code, use the second-to-last part
                    if (preg_match('/\d{5}/', $city_part)) {
                        // Reset array pointer to second-to-last element
                        end($address_parts);
                        prev($address_parts);
                        $city_part = trim(current($address_parts));
                    }
                    $property['city'] = $city_part;
                } else {
                    $property['city'] = trim($property['address']);
                }
            } else {
                $property['city'] = '';
            }
        }

        return $properties;
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
                  p.furniture_type, p.address, p.created_at, p.updated_at, p.image_url,
                  p.landlord_id, u.full_name as landlord_name,
                  (u.email || ',' || u.phone) as landlord_contact
                  FROM {$this->table} p
                  JOIN users u ON p.landlord_id = u.id
                  WHERE u.user_type = 'LANDLORD' AND p.id = ?";

        $stmt = $this->db->prepare($query);
        $stmt->execute([(int)$id]);

        $property = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($property) {
            // Process image URL (now stored directly in properties table)
            if ($property['image_url']) {
                $base_url = Config::get('base_url');
                // Extract filename from any existing URL format
                $filename = basename($property['image_url']);
                // Construct clean URL using current base URL
                $property['image_url'] = $base_url . '/uploads/properties/' . $filename;
            }

            // Extract just the city from the full address
            if (!empty($property['address'])) {
                $address_parts = explode(',', $property['address']);
                // Try to get the city - usually the last or second-to-last part
                if (count($address_parts) > 1) {
                    // Clean up whitespace and get the last part that looks like a city
                    $city_part = trim(end($address_parts));
                    // If the last part has postal code, use the second-to-last part
                    if (preg_match('/\d{5}/', $city_part)) {
                        // Reset array pointer to second-to-last element
                        end($address_parts);
                        prev($address_parts);
                        $city_part = trim(current($address_parts));
                    }
                    $property['city'] = $city_part;
                } else {
                    $property['city'] = trim($property['address']);
                }
            } else {
                $property['city'] = '';
            }
        }

        return $property;
    }



    /**
     * Create a new property
     *
     * @param array $data Property data
     * @return int|false The ID of the new property or false on failure
     */
    public function create($data) {
        // Validate landlord exists (now check users table directly)
        $landlord_check = "SELECT id FROM users WHERE id = ? AND user_type = 'LANDLORD'";
        $landlord_stmt = $this->db->prepare($landlord_check);
        $landlord_stmt->execute([(int)$data['landlord_id']]);
        $landlord_exists = $landlord_stmt->fetch(PDO::FETCH_ASSOC);

        if (!$landlord_exists) {
            return false;
        }

        $query = "INSERT INTO {$this->table} (
                    title, description, price, bedroom_count, bathroom_count,
                    furniture_type, landlord_id, address, image_url
                  ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        $stmt = $this->db->prepare($query);
        $result = $stmt->execute([
            $data['title'],
            $data['description'],
            (float)$data['price'],
            (int)$data['bedroom_count'],
            (int)$data['bathroom_count'],
            $data['furniture_type'],
            (int)$data['landlord_id'],
            $data['address'] ?? null,
            $data['image_url'] ?? null
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
        if (isset($data['address'])) {
            $set_clauses[] = "address = ?";
            $parameters[] = $data['address'];
        }





        if (isset($data['furniture_type'])) {
            $set_clauses[] = "furniture_type = ?";
            $parameters[] = $data['furniture_type'];
        }

        if (isset($data['landlord_id'])) {
            // Validate landlord exists (now check users table directly)
            $landlord_check = "SELECT id FROM users WHERE id = ? AND user_type = 'LANDLORD'";
            $landlord_stmt = $this->db->prepare($landlord_check);
            $landlord_stmt->execute([(int)$data['landlord_id']]);
            $landlord_exists = $landlord_stmt->fetch(PDO::FETCH_ASSOC);

            if (!$landlord_exists) {
                return false;
            }

            $set_clauses[] = "landlord_id = ?";
            $parameters[] = (int)$data['landlord_id'];
        }

        if (isset($data['image_url'])) {
            $set_clauses[] = "image_url = ?";
            $parameters[] = $data['image_url'];
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

        $query = "DELETE FROM {$this->table} WHERE id = ?";
        $stmt = $this->db->prepare($query);
        return $stmt->execute([(int)$id]);
    }
}
