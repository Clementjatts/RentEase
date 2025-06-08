<?php
/**
 * Property Controller
 *
 * Handles property-related API requests
 */
class PropertyController extends BaseController {
    private $property;

    /**
     * Constructor
     *
     * @param PDO $db Database connection
     * @param ResponseService $service Response service
     * @param array $request Request data
     */
    public function __construct($db, $service, $request) {
        parent::__construct($db, $service, $request);
        try {
            $this->property = new Property($db);
            
        } catch (Exception $e) {
            // Log the error and return a proper API response instead of PHP error
            error_log("PropertyController initialization error: " . $e->getMessage());
            $this->service->error("Error initializing controller", 500);
        }
    }



    /**
     * Get landlord ID for a given user ID (simplified - now user_id IS the landlord_id)
     *
     * @param int $user_id User ID
     * @return int|null Landlord ID or null if not found
     */
    private function getLandlordIdForUser($user_id) {
        try {
            // With simplified schema, user_id IS the landlord_id for LANDLORD users
            $query = "SELECT id FROM users WHERE id = ? AND user_type = 'LANDLORD'";
            $stmt = $this->db->prepare($query);
            $stmt->execute([$user_id]);
            $result = $stmt->fetch(PDO::FETCH_ASSOC);

            return $result ? (int)$result['id'] : null;
        } catch (Exception $e) {
            error_log("PropertyController::getLandlordIdForUser - Error getting landlord ID for user: " . $e->getMessage());
            return null;
        }
    }

    /**
     * Override processRequest to handle special image upload routing
     *
     * @return array Response data
     */
    public function processRequest() {
        $method = $this->request['method'];
        $action = $this->request['action'] ?? '';

        try {
            // Handle special image-related actions
            if ($method === 'POST' && $action === 'uploadImage') {
                return $this->uploadImage();
            }

            if ($method === 'GET' && $action === 'getImages') {
                $routeParams = $this->request['route_params'] ?? [];
                $propertyId = $routeParams['id'] ?? null;
                return $this->getImages($propertyId);
            }

            if ($method === 'DELETE' && $action === 'deleteImage') {
                $routeParams = $this->request['route_params'] ?? [];
                $imageId = $routeParams['id'] ?? null;
                return $this->deleteImage($imageId);
            }

            // Handle standard property actions
            if ($method === 'GET' && $action === 'getAll') {
                return $this->getAll();
            }

            if ($method === 'GET' && $action === 'getOne') {
                $routeParams = $this->request['route_params'] ?? [];
                $propertyId = $routeParams['id'] ?? null;
                return $this->getOne($propertyId);
            }

            // For all other requests, use the parent's default routing
            return parent::processRequest();

        } catch (Exception $e) {
            error_log("PropertyController::processRequest - Error processing request: " . $e->getMessage());
            return $this->service->serverError('Error processing request: ' . $e->getMessage());
        }
    }

    /**
     * Get all properties
     *
     * @return array
     */
    protected function getAll() {
        try {
            // Get pagination and query parameters
            $params = array_merge($this->getPaginationParams(), $this->getQueryParams());

            // If user is authenticated and is a LANDLORD, filter by their properties only
            $user_id = $this->getUserId();
            $user_type = $this->getUserType();

            if ($user_id && $user_type === 'LANDLORD') {
                // Get the landlord ID for this user
                $landlord_id = $this->getLandlordIdForUser($user_id);
                if ($landlord_id) {
                    $params['landlord_id'] = $landlord_id;
                }
            }

            // Get properties
            $properties = $this->property->getAll($params);

            // Get total count for pagination
            $total = $this->property->getCount($params);

            // Build response
            $data = [
                'properties' => $properties,
                'pagination' => [
                    'total' => $total,
                    'page' => $params['page'],
                    'limit' => $params['limit']
                ]
            ];

            return $this->service->success('Properties retrieved successfully', $data, 200, $total);
        } catch (Exception $e) {
            error_log("PropertyController::getAll - Error retrieving properties: " . $e->getMessage());
            return $this->service->error("Error retrieving properties: " . $e->getMessage(), 500);
        }
    }

    /**
     * Get one property by ID
     *
     * @param int $id Property ID
     * @return array
     */
    protected function getOne($id) {
        try {
            $property = $this->property->getById($id);

            if (!$property) {
                return $this->service->notFound('Property not found');
            }

            return $this->service->success('Property retrieved successfully', $property);
        } catch (Exception $e) {
            return $this->service->serverError('Error retrieving property: ' . $e->getMessage());
        }
    }

    /**
     * Create a new property
     *
     * @return array
     */
    protected function create() {
        try {

            $data = $this->getBody();

            // Validate required fields
            $required = ['title', 'landlord_id', 'price', 'bedroom_count', 'bathroom_count'];
            $validation_errors = $this->validateRequired($data, $required);

            if ($validation_errors) {
                return $this->service->badRequest('Validation failed', [
                    'errors' => $validation_errors
                ]);
            }

            // Set default values for optional fields
            $data['furniture_type'] = $data['furniture_type'] ?? 'unfurnished';

            // Create the property
            $id = $this->property->create($data);

            if (!$id) {
                return $this->service->serverError('Failed to create property');
            }

            // Get the created property
            $property = $this->property->getById($id);

            return $this->service->created($property, 'Property created successfully');
        } catch (Exception $e) {
            return $this->service->serverError('Error creating property: ' . $e->getMessage());
        }
    }

    /**
     * Update a property
     *
     * @param int $id Property ID
     * @param bool $partial Whether this is a partial update (PATCH)
     * @return array
     */
    protected function update($id, $partial = false) {
        try {
            // Check if property exists
            $property = $this->property->getById($id);

            if (!$property) {
                return $this->service->notFound('Property not found');
            }

            // Authorization: Only admins can edit any property, landlords can only edit their own
            $current_user_id = $this->getUserId();
            $is_admin = $this->isAdmin();
            $is_owner = ($current_user_id && $property['landlord_id'] == $current_user_id);

            if (!$is_admin && !$is_owner) {
                return $this->service->forbidden('Permission denied: You can only edit your own properties');
            }

            $data = $this->getBody();

            // For PUT requests (complete update), validate required fields
            if (!$partial) {
                $required = ['title', 'landlord_id', 'price', 'bedroom_count', 'bathroom_count'];
                $validation_errors = $this->validateRequired($data, $required);

                if ($validation_errors) {
                    return $this->service->badRequest('Validation failed', [
                        'errors' => $validation_errors
                    ]);
                }
            }

            // Update the property
            $result = $this->property->update($id, $data);

            if (!$result) {
                return $this->service->serverError('Failed to update property');
            }

            // Get the updated property
            $updated_property = $this->property->getById($id);

            return $this->service->success('Property updated successfully', $updated_property);
        } catch (Exception $e) {
            return $this->service->serverError('Error updating property: ' . $e->getMessage());
        }
    }

    /**
     * Delete a property
     *
     * @param int $id Property ID
     * @return array
     */
    protected function delete($id) {
        try {
            // Check if property exists
            $property = $this->property->getById($id);

            if (!$property) {
                return $this->service->notFound('Property not found');
            }

            // Authorization: Only admins can delete any property, landlords can only delete their own
            $current_user_id = $this->getUserId();
            $is_admin = $this->isAdmin();
            $is_owner = ($current_user_id && $property['landlord_id'] == $current_user_id);

            if (!$is_admin && !$is_owner) {
                return $this->service->forbidden('Permission denied: You can only delete your own properties');
            }

            // Delete property image if exists (simplified schema - image stored in properties table)
            if ($property['image_url']) {
                $filename = basename($property['image_url']);
                $filePath = __DIR__ . '/../uploads/properties/' . $filename;
                if (file_exists($filePath)) {
                    unlink($filePath); // Delete physical file
                }
            }

            // Delete the property
            $result = $this->property->delete($id);

            if (!$result) {
                return $this->service->serverError('Failed to delete property');
            }

            return $this->service->success('Property deleted successfully');
        } catch (Exception $e) {

            return $this->service->serverError('Error deleting property: ' . $e->getMessage());
        }
    }

    /**
     * Get property images (simplified - single image stored in properties table)
     *
     * @param int $id Property ID
     * @return array
     */
    protected function getImages($id) {
        try {
            // Validate property ID
            if (!is_numeric($id) || $id <= 0) {
                return $this->service->badRequest('Invalid property ID');
            }

            // Check if property exists
            $property = $this->property->getById($id);

            if (!$property) {
                return $this->service->notFound('Property not found');
            }

            // Return single image from property data
            $images = [];
            if ($property['image_url']) {
                $images[] = [
                    'id' => 1, // Single image always has ID 1
                    'property_id' => $id,
                    'image_url' => $property['image_url']
                ];
            }

            return $this->service->success('Property images retrieved successfully', [
                'images' => $images
            ]);
        } catch (Exception $e) {
            error_log("PropertyController::getImages - Error retrieving property images: " . $e->getMessage());
            return $this->service->serverError('Error retrieving property images: ' . $e->getMessage());
        }
    }

    /**
     * Upload property image
     *
     * @return array
     */
    protected function uploadImage() {
        try {
            // Get property ID from POST data (multipart form field)
            $propertyId = isset($_POST['property_id']) ? (int)$_POST['property_id'] : null;

            if (!$propertyId) {
                return $this->service->badRequest('Property ID is required');
            }

            // Check if property exists
            $property = $this->property->getById($propertyId);

            if (!$property) {
                return $this->service->notFound('Property not found');
            }

            // Check if file was uploaded
            if (!isset($_FILES['image'])) {
                error_log("PropertyController::uploadImage - No image file in request for property ID: $propertyId");
                return $this->service->badRequest('No image file uploaded');
            }

            $uploadError = $_FILES['image']['error'];
            if ($uploadError !== UPLOAD_ERR_OK) {
                $errorMessages = [
                    UPLOAD_ERR_INI_SIZE => 'File exceeds upload_max_filesize directive',
                    UPLOAD_ERR_FORM_SIZE => 'File exceeds MAX_FILE_SIZE directive',
                    UPLOAD_ERR_PARTIAL => 'File was only partially uploaded',
                    UPLOAD_ERR_NO_FILE => 'No file was uploaded',
                    UPLOAD_ERR_NO_TMP_DIR => 'Missing temporary folder',
                    UPLOAD_ERR_CANT_WRITE => 'Failed to write file to disk',
                    UPLOAD_ERR_EXTENSION => 'File upload stopped by extension'
                ];

                $errorMessage = $errorMessages[$uploadError] ?? 'Unknown upload error';
                error_log("PropertyController::uploadImage - File upload error: $errorMessage for property ID: $propertyId");
                return $this->service->badRequest('Upload error: ' . $errorMessage);
            }

            // Get file details
            $fileSize = $_FILES['image']['size'];
            $fileType = $_FILES['image']['type'];

            // Validate file size
            $maxFileSize = Config::get('max_file_size') ?? 5242880; // 5MB default
            if ($fileSize > $maxFileSize) {
                return $this->service->badRequest('File size exceeds maximum allowed size of ' . ($maxFileSize / 1024 / 1024) . 'MB');
            }

            // Validate file type
            $allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
            if (!in_array($fileType, $allowedTypes)) {
                return $this->service->badRequest('Invalid file type. Only JPEG, PNG, and GIF are allowed');
            }

            // Generate unique filename
            $extension = pathinfo($_FILES['image']['name'], PATHINFO_EXTENSION);
            $filename = uniqid('property_') . '.' . $extension;

            // Set upload directory (correctly mounted via Docker volume)
            $uploadDir = __DIR__ . '/../uploads/properties/';

            // Create directory if it doesn't exist
            if (!file_exists($uploadDir)) {
                if (!mkdir($uploadDir, 0755, true)) {
                    error_log("PropertyController::uploadImage - Failed to create upload directory for property ID: $propertyId");
                    return $this->service->serverError('Failed to create upload directory');
                }
            }

            // Check directory permissions
            if (!is_writable($uploadDir)) {
                error_log("PropertyController::uploadImage - Upload directory is not writable for property ID: $propertyId");
                return $this->service->serverError('Upload directory is not writable');
            }

            // Check available disk space
            $freeSpace = disk_free_space($uploadDir);
            if ($freeSpace !== false && $freeSpace < $fileSize * 2) { // Require 2x file size for safety
                error_log("PropertyController::uploadImage - Insufficient disk space for property ID: $propertyId");
                return $this->service->serverError('Insufficient disk space for upload');
            }

            $uploadPath = $uploadDir . $filename;

            // Move uploaded file
            if (!move_uploaded_file($_FILES['image']['tmp_name'], $uploadPath)) {
                error_log("PropertyController::uploadImage - Failed to move uploaded file for property ID: $propertyId");
                return $this->service->serverError('Failed to save uploaded file');
            }

            // Verify file was created and has correct size
            if (!file_exists($uploadPath) || filesize($uploadPath) !== $fileSize) {
                error_log("PropertyController::uploadImage - File verification failed after upload for property ID: $propertyId");
                return $this->service->serverError('File upload verification failed');
            }

            // File successfully uploaded to filesystem

            // Generate URL for the image
            $baseUrl = Config::get('base_url');
            $imageUrl = $baseUrl . '/uploads/properties/' . $filename;

            // Save image URL directly to properties table (simplified schema)
            try {
                $result = $this->property->update($propertyId, ['image_url' => $imageUrl]);

                if (!$result) {
                    error_log("PropertyController::uploadImage - Failed to save image URL to properties table for property ID: $propertyId");

                    // Clean up uploaded file since database save failed
                    if (file_exists($uploadPath)) {
                        unlink($uploadPath);
                    }

                    return $this->service->serverError('Failed to save image to database');
                }
            } catch (Exception $dbException) {
                error_log("PropertyController::uploadImage - Database exception while saving image URL: " . $dbException->getMessage());

                // Clean up uploaded file since database save failed
                if (file_exists($uploadPath)) {
                    unlink($uploadPath);
                }

                return $this->service->serverError('Database error while saving image');
            }

            // Image upload completed successfully

            return $this->service->success('Image uploaded successfully', [
                'id' => $propertyId, // Property ID since image is now part of property
                'url' => $imageUrl,
                'message' => 'Single image uploaded for property'
            ]);
        } catch (Exception $e) {
            error_log("PropertyController::uploadImage - Unexpected error during image upload: " . $e->getMessage());
            return $this->service->serverError('Error uploading image: ' . $e->getMessage());
        }
    }

    /**
     * Delete property image (simplified - clear image_url from properties table)
     *
     * @param int $propertyId Property ID
     * @return array
     */
    protected function deleteImage($propertyId) {
        try {
            // Validate property ID
            if (!is_numeric($propertyId) || $propertyId <= 0) {
                return $this->service->badRequest('Invalid property ID');
            }

            // Get property details before deletion
            $property = $this->property->getById($propertyId);
            if ($property && $property['image_url']) {
                // Try to delete physical file
                $filename = basename($property['image_url']);
                $filePath = __DIR__ . '/../uploads/properties/' . $filename;

                if (file_exists($filePath)) {
                    if (!unlink($filePath)) {
                        error_log("PropertyController::deleteImage - Failed to delete physical file for property ID: $propertyId");
                    }
                }
            }

            // Clear image URL from properties table
            $result = $this->property->update($propertyId, ['image_url' => null]);

            if (!$result) {
                error_log("PropertyController::deleteImage - Failed to clear image URL from properties table for property ID: $propertyId");
                return $this->service->serverError('Failed to delete image');
            }

            return $this->service->success('Image deleted successfully');
        } catch (Exception $e) {
            error_log("PropertyController::deleteImage - Error deleting image: " . $e->getMessage());
            return $this->service->serverError('Error deleting image: ' . $e->getMessage());
        }
    }



    /**
     * Get count of properties
     *
     * @return array
     */
    protected function getCount() {
        try {
            $params = $this->getQueryParams();
            $count = $this->property->getCount($params);

            return $this->service->success('Property count retrieved successfully', [
                'count' => $count
            ]);
        } catch (Exception $e) {
            return $this->service->serverError('Error retrieving property count: ' . $e->getMessage());
        }
    }
}
