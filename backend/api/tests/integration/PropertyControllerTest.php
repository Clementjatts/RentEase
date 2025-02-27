<?php
/**
 * Integration Tests for Property Controller
 */

class PropertyControllerTest extends ApiTestCase
{
    /**
     * Test getting all properties (public endpoint)
     */
    public function testGetAllProperties()
    {
        $response = $this->client->get('/properties');
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertTrue(is_array($data['data']));
        $this->assertTrue(count($data['data']) > 0);
    }
    
    /**
     * Test getting a specific property
     */
    public function testGetOneProperty()
    {
        $response = $this->client->get('/properties/1');
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertEquals(1, $data['data']['id']);
        $this->assertEquals('Cozy Downtown Apartment', $data['data']['title']);
    }
    
    /**
     * Test getting a non-existent property
     */
    public function testGetNonExistentProperty()
    {
        $response = $this->client->get('/properties/999');
        
        $this->assertEquals(404, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'not found') !== false);
    }
    
    /**
     * Test property search functionality
     */
    public function testPropertySearch()
    {
        $response = $this->client->get('/properties/search?city=Cityville&min_price=900&max_price=1500&bedrooms=2');
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertTrue(is_array($data['data']));
        
        // Check if at least one result matches search criteria
        if (count($data['data']) > 0) {
            $this->assertEquals('Cityville', $data['data'][0]['city']);
            $this->assertTrue($data['data'][0]['price'] >= 900);
            $this->assertTrue($data['data'][0]['price'] <= 1500);
            $this->assertEquals(2, $data['data'][0]['bedrooms']);
        }
    }
    
    /**
     * Test creating a property (requires authentication)
     */
    public function testCreateProperty()
    {
        $response = $this->apiRequest('POST', '/properties', [
            'json' => [
                'title' => 'New Test Property',
                'description' => 'A property created for testing',
                'price' => 1500.00,
                'address' => '123 Test St',
                'city' => 'Test City',
                'state' => 'Test State',
                'zip' => '12345',
                'bedrooms' => 3,
                'bathrooms' => 2,
                'area' => 1200,
                'property_type' => 'apartment',
                'landlord_id' => 1
            ]
        ], $this->authToken);
        
        $this->assertEquals(201, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertEquals('New Test Property', $data['data']['title']);
        $this->assertEquals(1500.00, $data['data']['price']);
        $this->assertEquals(3, $data['data']['bedrooms']);
    }
    
    /**
     * Test creating a property without authentication
     */
    public function testCreatePropertyWithoutAuth()
    {
        $response = $this->client->post('/properties', [
            'json' => [
                'title' => 'Unauthorized Property',
                'price' => 1000.00,
                'address' => '123 Test St',
                'city' => 'Test City',
                'state' => 'Test State',
                'zip' => '12345',
                'bedrooms' => 2,
                'bathrooms' => 1,
                'property_type' => 'apartment',
                'landlord_id' => 1
            ]
        ]);
        
        $this->assertEquals(401, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Unauthorized') !== false);
    }
    
    /**
     * Test creating a property with invalid data
     */
    public function testCreatePropertyWithInvalidData()
    {
        $response = $this->apiRequest('POST', '/properties', [
            'json' => [
                'title' => 'Invalid Property',
                // Missing required fields
            ]
        ]);
        
        $this->assertEquals(400, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'required') !== false);
    }
    
    /**
     * Test updating a property
     */
    public function testUpdateProperty()
    {
        $response = $this->apiRequest('PUT', '/properties/1', [
            'json' => [
                'title' => 'Updated Property Title',
                'price' => 1350.00
            ]
        ], $this->authToken);
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertEquals('Updated Property Title', $data['data']['title']);
        $this->assertEquals(1350.00, $data['data']['price']);
    }
    
    /**
     * Test updating a property that doesn't belong to the user
     */
    public function testUpdatePropertyUnauthorized()
    {
        // First create a property with the landlord account
        $createResponse = $this->apiRequest('POST', '/properties', [
            'json' => [
                'title' => 'Landlord Property',
                'description' => 'A property owned by a landlord',
                'price' => 1600.00,
                'address' => '456 Landlord St',
                'city' => 'Landlord City',
                'state' => 'Landlord State',
                'zip' => '54321',
                'bedrooms' => 4,
                'bathrooms' => 2.5,
                'area' => 1800,
                'property_type' => 'house',
                'landlord_id' => 1
            ]
        ], $this->adminAuthToken); // Using admin to create
        
        $createData = json_decode($createResponse->getBody(), true);
        $propertyId = $createData['data']['id'];
        
        // Now try to update with regular user
        $response = $this->apiRequest('PUT', "/properties/{$propertyId}", [
            'json' => [
                'title' => 'Hacked Property Title',
                'price' => 100.00
            ]
        ], $this->authToken);
        
        $this->assertEquals(403, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Permission') !== false);
    }
    
    /**
     * Test deleting a property
     */
    public function testDeleteProperty()
    {
        // First create a property to delete
        $createResponse = $this->apiRequest('POST', '/properties', [
            'json' => [
                'title' => 'Property to Delete',
                'description' => 'This property will be deleted',
                'price' => 1200.00,
                'address' => '789 Delete St',
                'city' => 'Delete City',
                'state' => 'Delete State',
                'zip' => '98765',
                'bedrooms' => 2,
                'bathrooms' => 1,
                'area' => 900,
                'property_type' => 'apartment',
                'landlord_id' => 1
            ]
        ], $this->authToken);
        
        $createData = json_decode($createResponse->getBody(), true);
        $propertyId = $createData['data']['id'];
        
        // Now delete the property
        $response = $this->apiRequest('DELETE', "/properties/{$propertyId}");
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(strpos($data['message'], 'deleted') !== false);
        
        // Try to get the deleted property
        $getResponse = $this->client->get("/properties/{$propertyId}");
        $this->assertEquals(404, $getResponse->getStatusCode());
    }
}
