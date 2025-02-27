<?php
/**
 * Unit Tests for Auth Middleware
 */

class AuthMiddlewareTest extends TestCase
{
    /**
     * Test JWT token generation
     */
    public function testGenerateToken()
    {
        $user = [
            'id' => 1,
            'username' => 'testuser',
            'email' => 'test@example.com',
            'role' => 'user'
        ];
        
        $auth = new AuthMiddleware($this->db);
        $token = $auth->generateToken($user);
        
        $this->assertTrue(is_string($token));
        $this->assertTrue(strpos($token, '.') !== false);
        
        // A JWT consists of three parts separated by dots
        $parts = explode('.', $token);
        $this->assertEquals(3, count($parts));
    }
    
    /**
     * Test authentication with valid token
     */
    public function testAuthenticateValidToken()
    {
        // Create a mock database statement and result
        $stmt = $this->createMock(PDOStatement::class);
        $stmt->method('fetch')->willReturn([
            'id' => 1,
            'username' => 'testuser',
            'email' => 'test@example.com',
            'role' => 'user'
        ]);
        $stmt->method('execute')->willReturn(true);
        
        // Create a mock database connection
        $db = $this->createMock(PDO::class);
        $db->method('prepare')->willReturn($stmt);
        
        // Create user data for token
        $user = [
            'id' => 1,
            'username' => 'testuser',
            'email' => 'test@example.com',
            'role' => 'user'
        ];
        
        // Create the auth middleware with the mock database
        $auth = new AuthMiddleware($db);
        
        // Generate a token
        $token = $auth->generateToken($user);
        
        // Set up server environment with the token
        $_SERVER['HTTP_AUTHORIZATION'] = 'Bearer ' . $token;
        
        // Test authentication
        $result = $auth->authenticate();
        
        $this->assertTrue(is_array($result));
        $this->assertEquals(1, $result['id']);
        $this->assertEquals('testuser', $result['username']);
        $this->assertEquals('test@example.com', $result['email']);
        $this->assertEquals('user', $result['role']);
        
        // Clean up
        unset($_SERVER['HTTP_AUTHORIZATION']);
    }
    
    /**
     * Test authentication with missing Authorization header
     */
    public function testAuthenticateMissingHeader()
    {
        $auth = new AuthMiddleware($this->db);
        
        // Ensure Authorization header is not set
        unset($_SERVER['HTTP_AUTHORIZATION']);
        
        $result = $auth->authenticate();
        
        $this->assertFalse($result);
    }
    
    /**
     * Test authentication with invalid token format
     */
    public function testAuthenticateInvalidTokenFormat()
    {
        $auth = new AuthMiddleware($this->db);
        
        // Set an invalid token format
        $_SERVER['HTTP_AUTHORIZATION'] = 'InvalidToken';
        
        $result = $auth->authenticate();
        
        $this->assertFalse($result);
        
        // Clean up
        unset($_SERVER['HTTP_AUTHORIZATION']);
    }
    
    /**
     * Test authentication with expired token
     */
    public function testAuthenticateExpiredToken()
    {
        // Create user data for token with expired timestamp
        $user = [
            'id' => 1,
            'username' => 'testuser',
            'email' => 'test@example.com',
            'role' => 'user'
        ];
        
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
                'id' => $user['id'],
                'username' => $user['username'],
                'email' => $user['email'],
                'role' => $user['role']
            ]
        ];
        
        // Generate an expired token
        $token = JWT::encode($payload, $jwt_config['secret_key'], 'HS256');
        
        // Set the expired token
        $_SERVER['HTTP_AUTHORIZATION'] = 'Bearer ' . $token;
        
        // Create the auth middleware
        $auth = new AuthMiddleware($this->db);
        
        // Test authentication with expired token
        $result = $auth->authenticate();
        
        $this->assertFalse($result);
        
        // Clean up
        unset($_SERVER['HTTP_AUTHORIZATION']);
    }
}
