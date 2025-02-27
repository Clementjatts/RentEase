<?php
/**
 * Favorite Controller
 * 
 * Handles favorite-related API requests
 */
class FavoriteController extends Controller {
    private $favorite;
    
    /**
     * Constructor
     * 
     * @param PDO $db Database connection
     * @param ResponseService $service Response service
     * @param array $request Request data
     */
    public function __construct($db, $service, $request) {
        parent::__construct($db, $service, $request);
        $this->favorite = new Favorite($db);
    }
    
    /**
     * Get all favorites
     * 
     * @return array
     */
    protected function getAll() {
        try {
            // Get pagination and query parameters
            $params = array_merge($this->getPaginationParams(), $this->getQueryParams());
            
            // Users can only view their own favorites, unless they're admin
            if (!$this->isAdmin()) {
                $params['user_id'] = $this->getUserId();
            }
            
            // Get favorites
            $favorites = $this->favorite->getAll($params);
            
            // Get total count for pagination
            $total = $this->favorite->getCount($params);
            
            // Build response
            $data = [
                'favorites' => $favorites,
                'pagination' => [
                    'total' => $total,
                    'page' => $params['page'],
                    'limit' => $params['limit'],
                    'pages' => ceil($total / $params['limit'])
                ]
            ];
            
            return $this->service->success('Favorites retrieved successfully', $data);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving favorites: ' . $e->getMessage());
        }
    }
    
    /**
     * Get one favorite by ID
     * 
     * @param int $id Favorite ID
     * @return array
     */
    protected function getOne($id) {
        try {
            $favorite = $this->favorite->getById($id);
            
            if (!$favorite) {
                return $this->service->notFound('Favorite not found');
            }
            
            // Users can only view their own favorites, unless they're admin
            if (!$this->isAdmin() && $favorite['user_id'] != $this->getUserId()) {
                return $this->service->forbidden('Permission denied');
            }
            
            return $this->service->success('Favorite retrieved successfully', $favorite);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving favorite: ' . $e->getMessage());
        }
    }
    
    /**
     * Create a new favorite
     * 
     * @return array
     */
    protected function create() {
        try {
            $data = $this->getBody();
            
            // Validate required fields
            $required = ['property_id'];
            $validation_errors = $this->validateRequired($data, $required);
            
            if ($validation_errors) {
                return $this->service->badRequest('Validation failed', [
                    'errors' => $validation_errors
                ]);
            }
            
            // Set user_id from authenticated user
            $data['user_id'] = $this->getUserId();
            
            // Create the favorite
            $id = $this->favorite->create($data);
            
            if ($id === -1) {
                return $this->service->conflict('Property is already in favorites');
            }
            
            if (!$id) {
                return $this->service->notFound('Property not found or failed to add to favorites');
            }
            
            // Get the created favorite
            $favorite = $this->favorite->getById($id);
            
            return $this->service->created('Property added to favorites', $favorite);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error adding to favorites: ' . $e->getMessage());
        }
    }
    
    /**
     * Update a favorite - Not implemented for favorites
     * 
     * @param int $id Favorite ID
     * @param bool $partial Whether this is a partial update (PATCH)
     * @return array
     */
    protected function update($id, $partial = false) {
        // Favorites don't have updatable fields
        return $this->service->methodNotAllowed('Update not allowed for favorites');
    }
    
    /**
     * Delete a favorite
     * 
     * @param int $id Favorite ID
     * @return array
     */
    protected function delete($id) {
        try {
            // Check if favorite exists
            $favorite = $this->favorite->getById($id);
            
            if (!$favorite) {
                return $this->service->notFound('Favorite not found');
            }
            
            // Users can only delete their own favorites, unless they're admin
            if (!$this->isAdmin() && $favorite['user_id'] != $this->getUserId()) {
                return $this->service->forbidden('Permission denied');
            }
            
            // Delete the favorite
            $result = $this->favorite->delete($id);
            
            if (!$result) {
                return $this->service->serverError('Failed to remove from favorites');
            }
            
            return $this->service->success('Property removed from favorites');
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error removing from favorites: ' . $e->getMessage());
        }
    }
    
    /**
     * Get count of favorites
     * 
     * @return array
     */
    protected function getCount() {
        try {
            $params = $this->getQueryParams();
            
            // Users can only count their own favorites, unless they're admin
            if (!$this->isAdmin()) {
                $params['user_id'] = $this->getUserId();
            }
            
            $count = $this->favorite->getCount($params);
            
            return $this->service->success('Favorite count retrieved successfully', [
                'count' => $count
            ]);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error retrieving favorite count: ' . $e->getMessage());
        }
    }
    
    /**
     * Check if a property is in user's favorites
     * 
     * @return array
     */
    public function check() {
        try {
            $property_id = isset($this->request['query']['property_id']) ? 
                (int)$this->request['query']['property_id'] : null;
            
            if (!$property_id) {
                return $this->service->badRequest('Property ID is required');
            }
            
            $user_id = $this->getUserId();
            $exists = $this->favorite->exists($user_id, $property_id);
            
            return $this->service->success('Favorite check completed', [
                'is_favorite' => $exists
            ]);
        } catch (Exception $e) {
            $this->logger->logError($e->getMessage());
            return $this->service->serverError('Error checking favorite status: ' . $e->getMessage());
        }
    }
}
