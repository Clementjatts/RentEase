<?php
/**
 * Base TestCase for RentEase Unit Tests
 */
use PHPUnit\Framework\TestCase as BaseTestCase;

class TestCase extends BaseTestCase
{
    /**
     * Database connection
     * @var PDO
     */
    protected $db;
    
    /**
     * Response service mock
     * @var ResponseService
     */
    protected $response;
    
    /**
     * Logger mock
     * @var Logger
     */
    protected $logger;
    
    /**
     * Set up the test environment
     */
    protected function setUp(): void
    {
        parent::setUp();
        
        // Create database connection
        $this->db = $this->getDatabaseConnection();
        
        // Set up response service mock
        $this->response = $this->createMock(ResponseService::class);
        
        // Set up logger mock
        $this->logger = $this->createMock(Logger::class);
    }
    
    /**
     * Get a database connection
     * 
     * @return PDO
     */
    protected function getDatabaseConnection()
    {
        try {
            $db = new PDO(
                'mysql:host=' . getenv('DB_HOST') . ';dbname=' . getenv('DB_NAME'),
                getenv('DB_USER'),
                getenv('DB_PASS')
            );
            $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            return $db;
        } catch (PDOException $e) {
            $this->fail('Database connection failed: ' . $e->getMessage());
        }
    }
    
    /**
     * Create a mock request array
     * 
     * @param string $method HTTP method
     * @param string $path Request path
     * @param array $body Request body
     * @param array $query Query parameters
     * @param array $headers Request headers
     * @return array
     */
    protected function createMockRequest($method, $path, $body = [], $query = [], $headers = [])
    {
        return [
            'method' => $method,
            'path' => $path,
            'path_parts' => explode('/', trim($path, '/')),
            'query' => $query,
            'body' => $body,
            'headers' => $headers
        ];
    }
    
    /**
     * Create a mock authenticated user
     * 
     * @param int $id User ID
     * @param string $role User role
     * @return array
     */
    protected function createMockUser($id = 1, $role = 'user')
    {
        return [
            'id' => $id,
            'username' => 'testuser',
            'email' => 'test@example.com',
            'role' => $role
        ];
    }
    
    /**
     * Tear down the test environment
     */
    protected function tearDown(): void
    {
        $this->db = null;
        parent::tearDown();
    }
}
