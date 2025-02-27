<?php
/**
 * Integration Tests for Auth Controller
 */

class AuthControllerTest extends ApiTestCase
{
    /**
     * Test user login with valid credentials
     */
    public function testLoginSuccess()
    {
        $response = $this->client->post('/auth/login', [
            'json' => [
                'email' => 'user@example.com',
                'password' => 'password'
            ]
        ]);
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertTrue(array_key_exists('token', $data['data']));
        $this->assertTrue(array_key_exists('user', $data['data']));
        $this->assertEquals(1, $data['data']['user']['id']);
        $this->assertEquals('testuser', $data['data']['user']['username']);
    }
    
    /**
     * Test user login with invalid credentials
     */
    public function testLoginFailure()
    {
        $response = $this->client->post('/auth/login', [
            'json' => [
                'email' => 'user@example.com',
                'password' => 'wrongpassword'
            ]
        ]);
        
        $this->assertEquals(401, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Invalid') !== false);
    }
    
    /**
     * Test user login with missing fields
     */
    public function testLoginMissingFields()
    {
        $response = $this->client->post('/auth/login', [
            'json' => [
                'email' => 'user@example.com'
                // Password is missing
            ]
        ]);
        
        $this->assertEquals(400, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'required') !== false);
    }
    
    /**
     * Test user registration
     */
    public function testRegisterSuccess()
    {
        $response = $this->client->post('/auth/register', [
            'json' => [
                'username' => 'newuser',
                'email' => 'newuser@example.com',
                'password' => 'password123',
                'password_confirm' => 'password123'
            ]
        ]);
        
        $this->assertEquals(201, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertTrue(array_key_exists('user', $data['data']));
        $this->assertEquals('newuser', $data['data']['user']['username']);
    }
    
    /**
     * Test user registration with existing email
     */
    public function testRegisterDuplicate()
    {
        $response = $this->client->post('/auth/register', [
            'json' => [
                'username' => 'anotheruser',
                'email' => 'user@example.com', // This email already exists
                'password' => 'password123',
                'password_confirm' => 'password123'
            ]
        ]);
        
        $this->assertEquals(409, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'already exists') !== false);
    }
    
    /**
     * Test user registration with password mismatch
     */
    public function testRegisterPasswordMismatch()
    {
        $response = $this->client->post('/auth/register', [
            'json' => [
                'username' => 'anotheruser',
                'email' => 'another@example.com',
                'password' => 'password123',
                'password_confirm' => 'differentpassword'
            ]
        ]);
        
        $this->assertEquals(400, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'match') !== false);
    }
    
    /**
     * Test get current user with valid token
     */
    public function testGetCurrentUserSuccess()
    {
        $response = $this->apiRequest('GET', '/auth/me');
        
        $this->assertEquals(200, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('success', $data['status']);
        $this->assertTrue(array_key_exists('data', $data));
        $this->assertEquals(1, $data['data']['id']);
        $this->assertEquals('testuser', $data['data']['username']);
    }
    
    /**
     * Test get current user without authentication
     */
    public function testGetCurrentUserUnauthorized()
    {
        // Make request without auth token
        $response = $this->client->get('/auth/me');
        
        $this->assertEquals(401, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Unauthorized') !== false);
    }
}
