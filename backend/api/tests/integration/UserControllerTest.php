<?php
/**
 * Integration Tests for User Controller
 */

class UserControllerTest extends ApiTestCase
{
    /**
     * Test getting all users with admin access
     */
    public function testGetAllUsersAsAdmin()
    {
        $response = $this->apiRequest('GET', '/users', [], $this->adminAuthToken);
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertArrayHasKey('data', $data);
        $this->assertTrue(is_array($data['data']));
        $this->assertTrue(count($data['data']) > 0);
    }
    
    /**
     * Test getting all users with regular user (should be forbidden)
     */
    public function testGetAllUsersAsRegularUser()
    {
        $response = $this->apiRequest('GET', '/users');
        
        $this->assertEquals(403, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Permission') !== false);
    }
    
    /**
     * Test getting a specific user
     */
    public function testGetOneUser()
    {
        $response = $this->apiRequest('GET', '/users/1');
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertArrayHasKey('data', $data);
        $this->assertEquals(1, $data['data']['id']);
        $this->assertEquals('testuser', $data['data']['username']);
    }
    
    /**
     * Test getting a non-existent user
     */
    public function testGetNonExistentUser()
    {
        $response = $this->apiRequest('GET', '/users/999');
        
        $this->assertEquals(404, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'not found') !== false);
    }
    
    /**
     * Test updating own user profile
     */
    public function testUpdateOwnProfile()
    {
        $response = $this->apiRequest('PUT', '/users/1', [
            'json' => [
                'username' => 'updated_username',
                'email' => 'updated@example.com'
            ]
        ]);
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertArrayHasKey('data', $data);
        $this->assertEquals('updated_username', $data['data']['username']);
        $this->assertEquals('updated@example.com', $data['data']['email']);
    }
    
    /**
     * Test updating another user's profile (should be forbidden for regular users)
     */
    public function testUpdateOtherUserProfile()
    {
        $response = $this->apiRequest('PUT', '/users/2', [
            'json' => [
                'username' => 'hacked_admin',
                'email' => 'hacked@example.com'
            ]
        ]);
        
        $this->assertEquals(403, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Permission') !== false);
    }
    
    /**
     * Test updating user with admin privileges
     */
    public function testUpdateUserAsAdmin()
    {
        $response = $this->apiRequest('PUT', '/users/1', [
            'json' => [
                'username' => 'admin_updated',
                'email' => 'admin_updated@example.com',
                'role' => 'admin' // Only admins can change roles
            ]
        ], $this->adminAuthToken);
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertArrayHasKey('data', $data);
        $this->assertEquals('admin_updated', $data['data']['username']);
        $this->assertEquals('admin_updated@example.com', $data['data']['email']);
        $this->assertEquals('admin', $data['data']['role']);
    }
    
    /**
     * Test updating user with invalid data
     */
    public function testUpdateUserWithInvalidData()
    {
        $response = $this->apiRequest('PUT', '/users/1', [
            'json' => [
                'email' => 'not-an-email'
            ]
        ]);
        
        $this->assertEquals(400, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'invalid') !== false);
    }
    
    /**
     * Test accessing user endpoints without authentication
     */
    public function testUserEndpointsWithoutAuth()
    {
        // Test GET /users/1 without auth
        $response = $this->client->get('/users/1');
        
        $this->assertEquals(401, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Unauthorized') !== false);
        
        // Test PUT /users/1 without auth
        $response = $this->client->put('/users/1', [
            'json' => [
                'username' => 'unauthorized_update'
            ]
        ]);
        
        $this->assertEquals(401, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Unauthorized') !== false);
    }
}
