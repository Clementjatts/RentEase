<?php
/**
 * Unit Tests for Response Service
 */

// Load mock service
require_once __DIR__ . '/../mocks/MockResponseService.php';

class ResponseServiceTest extends TestCase
{
    /**
     * Response service instance
     * @var MockResponseService
     */
    private $responseService;
    
    /**
     * Set up the test environment
     */
    protected function setUp(): void
    {
        parent::setUp();
        
        // Create a new MockResponseService
        $this->responseService = new MockResponseService();
    }
    
    /**
     * Test success response
     */
    public function testSuccess()
    {
        $message = 'Success message';
        $data = ['key' => 'value'];
        
        $response = $this->responseService->success($message, $data);
        
        $this->assertTrue(is_array($response));
        $this->assertTrue(array_key_exists('status', $response));
        $this->assertTrue(array_key_exists('message', $response));
        $this->assertTrue(array_key_exists('data', $response));
        $this->assertEquals('success', $response['status']);
        $this->assertEquals($message, $response['message']);
        $this->assertEquals($data, $response['data']);
        $this->assertEquals(200, $this->responseService->statusCode);
    }
    
    /**
     * Test error response
     */
    public function testError()
    {
        $message = 'Error message';
        $errors = ['field' => 'Invalid value'];
        
        $response = $this->responseService->error($message, $errors);
        
        $this->assertTrue(is_array($response));
        $this->assertTrue(array_key_exists('status', $response));
        $this->assertTrue(array_key_exists('message', $response));
        $this->assertTrue(array_key_exists('errors', $response));
        $this->assertEquals('error', $response['status']);
        $this->assertEquals($message, $response['message']);
        $this->assertEquals($errors, $response['errors']);
        $this->assertEquals(400, $this->responseService->statusCode);
    }
    
    /**
     * Test created response
     */
    public function testCreated()
    {
        $message = 'Resource created';
        $data = ['id' => 1];
        
        $response = $this->responseService->created($message, $data);
        
        $this->assertTrue(is_array($response));
        $this->assertTrue(array_key_exists('status', $response));
        $this->assertTrue(array_key_exists('message', $response));
        $this->assertTrue(array_key_exists('data', $response));
        $this->assertEquals('success', $response['status']);
        $this->assertEquals($message, $response['message']);
        $this->assertEquals($data, $response['data']);
        $this->assertEquals(201, $this->responseService->statusCode);
    }
    
    /**
     * Test not found response
     */
    public function testNotFound()
    {
        $message = 'Resource not found';
        
        $response = $this->responseService->notFound($message);
        
        $this->assertTrue(is_array($response));
        $this->assertTrue(array_key_exists('status', $response));
        $this->assertTrue(array_key_exists('message', $response));
        $this->assertEquals('error', $response['status']);
        $this->assertEquals($message, $response['message']);
        $this->assertEquals(404, $this->responseService->statusCode);
    }
    
    /**
     * Test bad request response
     */
    public function testBadRequest()
    {
        $message = 'Bad request';
        $errors = ['field' => 'Required'];
        
        $response = $this->responseService->badRequest($message, $errors);
        
        $this->assertTrue(is_array($response));
        $this->assertTrue(array_key_exists('status', $response));
        $this->assertTrue(array_key_exists('message', $response));
        $this->assertTrue(array_key_exists('errors', $response));
        $this->assertEquals('error', $response['status']);
        $this->assertEquals($message, $response['message']);
        $this->assertEquals($errors, $response['errors']);
        $this->assertEquals(400, $this->responseService->statusCode);
    }
    
    /**
     * Test unauthorized response
     */
    public function testUnauthorized()
    {
        $message = 'Unauthorized access';
        
        $response = $this->responseService->unauthorized($message);
        
        $this->assertTrue(is_array($response));
        $this->assertTrue(array_key_exists('status', $response));
        $this->assertTrue(array_key_exists('message', $response));
        $this->assertEquals('error', $response['status']);
        $this->assertEquals($message, $response['message']);
        $this->assertEquals(401, $this->responseService->statusCode);
    }
    
    /**
     * Test forbidden response
     */
    public function testForbidden()
    {
        $message = 'Access forbidden';
        
        $response = $this->responseService->forbidden($message);
        
        $this->assertTrue(is_array($response));
        $this->assertTrue(array_key_exists('status', $response));
        $this->assertTrue(array_key_exists('message', $response));
        $this->assertEquals('error', $response['status']);
        $this->assertEquals($message, $response['message']);
        $this->assertEquals(403, $this->responseService->statusCode);
    }
    
    /**
     * Test method not allowed response
     */
    public function testMethodNotAllowed()
    {
        $message = 'Method not allowed';
        
        $response = $this->responseService->methodNotAllowed($message);
        
        $this->assertTrue(is_array($response));
        $this->assertTrue(array_key_exists('status', $response));
        $this->assertTrue(array_key_exists('message', $response));
        $this->assertEquals('error', $response['status']);
        $this->assertEquals($message, $response['message']);
        $this->assertEquals(405, $this->responseService->statusCode);
    }
    
    /**
     * Test conflict response
     */
    public function testConflict()
    {
        $message = 'Resource conflict';
        
        $response = $this->responseService->conflict($message);
        
        $this->assertTrue(is_array($response));
        $this->assertTrue(array_key_exists('status', $response));
        $this->assertTrue(array_key_exists('message', $response));
        $this->assertEquals('error', $response['status']);
        $this->assertEquals($message, $response['message']);
        $this->assertEquals(409, $this->responseService->statusCode);
    }
    
    /**
     * Test server error response
     */
    public function testServerError()
    {
        $message = 'Internal server error';
        
        $response = $this->responseService->serverError($message);
        
        $this->assertTrue(is_array($response));
        $this->assertTrue(array_key_exists('status', $response));
        $this->assertTrue(array_key_exists('message', $response));
        $this->assertEquals('error', $response['status']);
        $this->assertEquals($message, $response['message']);
        $this->assertEquals(500, $this->responseService->statusCode);
    }
}
