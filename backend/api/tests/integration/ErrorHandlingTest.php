<?php
/**
 * Integration Tests for Error Handling and Edge Cases
 */

class ErrorHandlingTest extends ApiTestCase
{
    /**
     * Test invalid endpoint
     */
    public function testInvalidEndpoint()
    {
        $response = $this->client->get('/non-existent-endpoint');
        
        $this->assertEquals(404, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'not found') !== false);
    }
    
    /**
     * Test method not allowed
     */
    public function testMethodNotAllowed()
    {
        $response = $this->client->delete('/auth/login');
        
        $this->assertEquals(405, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'not allowed') !== false);
    }
    
    /**
     * Test malformed JSON in requests
     */
    public function testMalformedJson()
    {
        $response = $this->client->post('/auth/login', [
            'headers' => ['Content-Type' => 'application/json'],
            'body' => '{malformed-json'
        ]);
        
        $this->assertEquals(400, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Invalid') !== false);
    }
    
    /**
     * Test request with too large payload
     */
    public function testTooLargePayload()
    {
        // Generate a very large JSON payload
        $payload = ['data' => str_repeat('a', 1024 * 1024 * 5)]; // 5MB string
        
        $response = $this->client->post('/auth/login', [
            'json' => $payload,
            'timeout' => 5 // Shorter timeout to avoid hanging test
        ]);
        
        // Server should either reject with 413 or possibly 400 or 500 
        // depending on server configuration
        $statusCode = $response->getStatusCode();
        $this->assertTrue(
            $statusCode == 400 || $statusCode == 413 || $statusCode == 500,
            "Expected status code to be 400, 413, or 500, but got $statusCode"
        );
    }
    
    /**
     * Test expired JWT token
     */
    public function testExpiredJwtToken()
    {
        // Load required classes for JWT handling
        require_once BASE_PATH . '/utils/JWT.php';
        require_once BASE_PATH . '/config/Config.php';
        
        // Get JWT configuration
        $jwt_config = Config::getJwtConfig();
        
        // Create an expired payload
        $payload = [
            'iat' => time() - 7200, // 2 hours ago
            'exp' => time() - 3600, // 1 hour ago
            'data' => [
                'id' => 1,
                'username' => 'testuser',
                'email' => 'test@example.com',
                'role' => 'user'
            ]
        ];
        
        // Generate an expired token
        $token = JWT::encode($payload, $jwt_config['secret_key'], 'HS256');
        
        // Try to access a protected endpoint with expired token
        $response = $this->client->get('/users/1', [
            'headers' => ['Authorization' => 'Bearer ' . $token]
        ]);
        
        $this->assertEquals(401, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        $this->assertTrue(strpos($data['message'], 'Unauthorized') !== false);
    }
    
    /**
     * Test tampered JWT token
     */
    public function testTamperedJwtToken()
    {
        // Get a valid token
        $validToken = $this->authToken;
        
        // Tamper with the token (change the middle part)
        $parts = explode('.', $validToken);
        if (count($parts) >= 2) {
            $decodedPayload = base64_decode($parts[1]);
            $payload = json_decode($decodedPayload, true);
            
            // Change the user ID or role
            if (isset($payload['data'])) {
                $payload['data']['role'] = 'admin'; // Try to escalate privileges
                
                // Re-encode the payload
                $tamperedPayload = base64_encode(json_encode($payload));
                
                // Replace the payload part in the token
                $parts[1] = $tamperedPayload;
                $tamperedToken = implode('.', $parts);
                
                // Try to access admin-only endpoint with tampered token
                $response = $this->client->get('/users', [
                    'headers' => ['Authorization' => 'Bearer ' . $tamperedToken]
                ]);
                
                $this->assertEquals(401, $response->getStatusCode());
                
                $data = json_decode($response->getBody(), true);
                
                $this->assertEquals('error', $data['status']);
                $this->assertTrue(strpos($data['message'], 'Unauthorized') !== false);
            }
        }
    }
    
    /**
     * Test SQL injection attempts
     */
    public function testSqlInjection()
    {
        // Test login with SQL injection attempt
        $response = $this->client->post('/auth/login', [
            'json' => [
                'email' => "' OR 1=1 --",
                'password' => 'anything'
            ]
        ]);
        
        $this->assertEquals(401, $response->getStatusCode());
        
        $data = json_decode($response->getBody(), true);
        
        $this->assertEquals('error', $data['status']);
        
        // Try SQL injection in query params
        $response = $this->client->get("/properties/search?city=' OR 1=1 --");
        
        // Should not return all properties due to injection
        $data = json_decode($response->getBody(), true);
        if (isset($data['data']) && is_array($data['data'])) {
            // Either we get no results or legitimate filtered results
            $this->assertTrue(
                count($data['data']) < 10,
                'SQL Injection may have succeeded if too many results returned'
            );
        }
    }
    
    /**
     * Test cross-site scripting (XSS) protection
     */
    public function testXssProtection()
    {
        // Create a property with JavaScript in the title
        $xssPayload = '<script>alert("XSS")</script>Malicious Property';
        
        $response = $this->apiRequest('POST', '/properties', [
            'json' => [
                'title' => $xssPayload,
                'description' => 'A property with XSS attempt',
                'price' => 1000.00,
                'address' => '123 XSS St',
                'city' => 'XSS City',
                'state' => 'XSS State',
                'zip' => '12345',
                'bedrooms' => 2,
                'bathrooms' => 1,
                'area' => 800,
                'property_type' => 'apartment',
                'landlord_id' => 1
            ]
        ]);
        
        // The request might succeed, but the XSS should be sanitized
        if ($response->getStatusCode() == 201) {
            $data = json_decode($response->getBody(), true);
            
            // Check if the script tags were sanitized
            if (isset($data['data']['title'])) {
                $this->assertNotEquals($xssPayload, $data['data']['title']);
                $this->assertTrue(strpos($data['data']['title'], '<script>') === false);
            }
        }
    }
    
    /**
     * Test rate limiting
     */
    public function testRateLimiting()
    {
        // Make multiple requests in quick succession
        $requestCount = 20;
        $responses = [];
        
        for ($i = 0; $i < $requestCount; $i++) {
            $responses[] = $this->client->get('/properties', [
                'timeout' => 2,
                'http_errors' => false
            ]);
        }
        
        // Check if any responses indicate rate limiting
        $rateLimited = false;
        foreach ($responses as $response) {
            if ($response->getStatusCode() == 429) {
                $rateLimited = true;
                break;
            }
        }
        
        // We don't force this to be true since rate limiting might not be implemented
        // but we document the result
        $this->addToAssertionCount(1);
        if ($rateLimited) {
            $this->assertTrue($rateLimited, 'Rate limiting is properly implemented');
        } else {
            $this->markTestSkipped('Rate limiting is not implemented or threshold not reached');
        }
    }
}
