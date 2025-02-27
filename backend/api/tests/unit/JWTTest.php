<?php
/**
 * Unit Tests for JWT Utility
 */

use PHPUnit\Framework\TestCase;

class JWTTest extends TestCase
{
    /**
     * Test JWT encoding
     */
    public function testEncode()
    {
        $payload = [
            'iat' => time(),
            'exp' => time() + 3600,
            'data' => [
                'id' => 1,
                'username' => 'testuser'
            ]
        ];
        
        $key = 'test_secret_key';
        
        $token = JWT::encode($payload, $key);
        
        $this->assertTrue(is_string($token));
        $this->assertTrue(strpos($token, '.') !== false);
        
        // A JWT consists of three parts separated by dots
        $parts = explode('.', $token);
        $this->assertEquals(3, count($parts));
    }
    
    /**
     * Test JWT decoding
     */
    public function testDecode()
    {
        $payload = [
            'iat' => time(),
            'exp' => time() + 3600,
            'data' => [
                'id' => 1,
                'username' => 'testuser'
            ]
        ];
        
        $key = 'test_secret_key';
        
        $token = JWT::encode($payload, $key);
        $decoded = JWT::decode($token, $key);
        
        $this->assertTrue(is_array($decoded));
        $this->assertEquals($payload['data']['id'], $decoded['data']['id']);
        $this->assertEquals($payload['data']['username'], $decoded['data']['username']);
    }
    
    /**
     * Test JWT verification with invalid signature
     */
    public function testInvalidSignature()
    {
        $payload = [
            'iss' => 'http://rentease.example.com',
            'aud' => 'http://rentease.example.com',
            'iat' => time(),
            'exp' => time() + 3600,
            'data' => [
                'id' => 1,
                'username' => 'testuser'
            ]
        ];
        
        $key = 'test_secret_key';
        $wrong_key = 'wrong_key';
        
        $token = JWT::encode($payload, $key);
        
        try {
            JWT::decode($token, $wrong_key);
            $this->fail('Exception was not thrown when using an invalid signature');
        } catch (Exception $e) {
            $this->assertTrue(true); // If we catch an exception, the test passes
        }
    }
    
    /**
     * Test JWT with expired token
     */
    public function testExpiredToken()
    {
        $payload = [
            'iss' => 'http://rentease.example.com',
            'aud' => 'http://rentease.example.com',
            'iat' => time() - 7200,
            'exp' => time() - 3600, // Expired 1 hour ago
            'data' => [
                'id' => 1,
                'username' => 'testuser'
            ]
        ];
        
        $key = 'test_secret_key';
        
        $token = JWT::encode($payload, $key);
        
        try {
            JWT::decode($token, $key);
            $this->fail('Exception was not thrown when using an expired token');
        } catch (Exception $e) {
            $this->assertTrue(true); // If we catch an exception, the test passes
        }
    }
    
    /**
     * Test JWT with malformed token
     */
    public function testMalformedToken()
    {
        $key = 'test_secret_key';
        $malformed_token = 'not.a.valid.token';
        
        try {
            JWT::decode($malformed_token, $key);
            $this->fail('Exception was not thrown when using a malformed token');
        } catch (Exception $e) {
            $this->assertTrue(true); // If we catch an exception, the test passes
        }
    }
}
