<?php
/**
 * Integration Tests for Favorite Controller
 */

class FavoriteControllerTest extends ApiTestCase
{
    /**
     * Test getting all favorites for the authenticated user
     */
    public function testGetAllFavorites()
    {
        $response = $this->apiRequest('GET', '/favorites');
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertTrue(is_array($data['data']));
    }
    
    /**
     * Test getting a specific favorite
     */
    public function testGetOneFavorite()
    {
        $response = $this->apiRequest('GET', '/favorites/1');
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertEquals(1, $data['data']['id']);
    }
    
    /**
     * Test getting a non-existent favorite
     */
    public function testGetNonExistentFavorite()
    {
        $response = $this->apiRequest('GET', '/favorites/999');
        
        $this->assertEquals(404, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'not found') !== false);
    }
    
    /**
     * Test getting favorites by user
     */
    public function testGetFavoritesByUser()
    {
        $response = $this->apiRequest('GET', '/favorites/user/1');
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertTrue(is_array($data['data']));
        
        // Check if the favorites belong to the correct user
        if (count($data['data']) > 0) {
            $this->assertEquals(1, $data['data'][0]['user_id']);
        }
    }
    
    /**
     * Test getting favorites by property
     */
    public function testGetFavoritesByProperty()
    {
        $response = $this->apiRequest('GET', '/favorites/property/1');
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertTrue(is_array($data['data']));
        
        // Check if the favorites are for the correct property
        if (count($data['data']) > 0) {
            $this->assertEquals(1, $data['data'][0]['property_id']);
        }
    }
    
    /**
     * Test creating a favorite
     */
    public function testCreateFavorite()
    {
        // First, make sure we have at least one property that's not already favorited
        $propertyId = 3; // Using property ID 3 which might not be favorited yet
        
        $response = $this->apiRequest('POST', '/favorites', [
            'json' => [
                'property_id' => $propertyId
            ]
        ]);
        
        $this->assertEquals(201, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertEquals($propertyId, $data['data']['property_id']);
        $this->assertEquals(1, $data['data']['user_id']); // User ID from auth token
    }
    
    /**
     * Test creating a duplicate favorite (should fail)
     */
    public function testCreateDuplicateFavorite()
    {
        // Use property ID 1 which is already favorited by test user
        $response = $this->apiRequest('POST', '/favorites', [
            'json' => [
                'property_id' => 1
            ]
        ]);
        
        $this->assertEquals(409, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'already') !== false);
    }
    
    /**
     * Test creating a favorite for a non-existent property
     */
    public function testCreateFavoriteInvalidProperty()
    {
        $response = $this->apiRequest('POST', '/favorites', [
            'json' => [
                'property_id' => 999 // Non-existent property
            ]
        ]);
        
        $this->assertEquals(400, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Invalid') !== false);
    }
    
    /**
     * Test deleting a favorite
     */
    public function testDeleteFavorite()
    {
        // First create a favorite to delete
        $createResponse = $this->apiRequest('POST', '/favorites', [
            'json' => [
                'property_id' => 2 // Assuming this is a valid property
            ]
        ]);
        
        $createData = json_decode($createResponse->getBody(), true);
        $favoriteId = $createData['data']['id'];
        
        // Now delete the favorite
        $response = $this->apiRequest('DELETE', "/favorites/{$favoriteId}");
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(strpos($data['message'], 'deleted') !== false);
        
        // Try to get the deleted favorite
        $getResponse = $this->apiRequest('GET', "/favorites/{$favoriteId}");
        $this->assertEquals(404, $getResponse->getStatusCode());
    }
    
    /**
     * Test deleting another user's favorite (should be forbidden)
     */
    public function testDeleteOtherUserFavorite()
    {
        // Create a favorite with admin user
        $createResponse = $this->apiRequest('POST', '/favorites', [
            'json' => [
                'property_id' => 3 // Assuming this is a valid property
            ]
        ], $this->adminAuthToken);
        
        $createData = json_decode($createResponse->getBody(), true);
        $favoriteId = $createData['data']['id'];
        
        // Try to delete with regular user
        $response = $this->apiRequest('DELETE', "/favorites/{$favoriteId}");
        
        $this->assertEquals(403, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Permission') !== false);
    }
    
    /**
     * Test accessing favorite endpoints without authentication
     */
    public function testFavoriteEndpointsWithoutAuth()
    {
        // Test GET /favorites without auth
        $response = $this->client->get('/favorites');
        
        $this->assertEquals(401, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Unauthorized') !== false);
        
        // Test POST /favorites without auth
        $response = $this->client->post('/favorites', [
            'json' => [
                'property_id' => 1
            ]
        ]);
        
        $this->assertEquals(401, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Unauthorized') !== false);
    }
}
