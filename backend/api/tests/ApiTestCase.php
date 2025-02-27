<?php
/**
 * Base ApiTestCase for RentEase API Integration Tests
 */
use GuzzleHttp\Client;
use GuzzleHttp\Exception\GuzzleException;
use PHPUnit\Framework\TestCase as BaseTestCase;

class ApiTestCase extends BaseTestCase
{
    /**
     * HTTP client
     * @var Client
     */
    protected $client;
    
    /**
     * API base URL
     * @var string
     */
    protected $baseUrl;
    
    /**
     * Authentication token for authorized requests
     * @var string
     */
    protected $authToken;
    
    /**
     * Admin authentication token
     * @var string
     */
    protected $adminAuthToken;
    
    /**
     * Database connection
     * @var PDO
     */
    protected $db;
    
    /**
     * Set up the test environment
     */
    protected function setUp(): void
    {
        parent::setUp();
        
        // Set up HTTP client
        $this->baseUrl = getenv('API_URL');
        $this->client = new Client([
            'base_uri' => $this->baseUrl,
            'http_errors' => false
        ]);
        
        // Create database connection
        $this->db = $this->getDatabaseConnection();
        
        // Authenticate as a regular user and admin for tests that need it
        $this->authToken = $this->getAuthToken('user@example.com', 'password');
        $this->adminAuthToken = $this->getAuthToken('admin@example.com', 'admin123');
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
     * Get authentication token for a user
     * 
     * @param string $email User email
     * @param string $password User password
     * @return string JWT token
     */
    protected function getAuthToken($email, $password)
    {
        try {
            $response = $this->client->post('/auth/login', [
                'json' => [
                    'email' => $email,
                    'password' => $password
                ]
            ]);
            
            $data = json_decode($response->getBody(), true);
            if (isset($data['data']['token'])) {
                return $data['data']['token'];
            }
            
            // If we couldn't get a token, attempt to use the test database directly
            $stmt = $this->db->prepare("SELECT id, username, email, role FROM users WHERE email = ?");
            $stmt->execute([$email]);
            $user = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if ($user) {
                require_once BASE_PATH . '/middleware/AuthMiddleware.php';
                $auth = new AuthMiddleware($this->db);
                return $auth->generateToken($user);
            }
            
            $this->fail('Could not get authentication token');
        } catch (GuzzleException $e) {
            $this->fail('Auth request failed: ' . $e->getMessage());
        }
    }
    
    /**
     * Make an authenticated API request
     * 
     * @param string $method HTTP method
     * @param string $uri Request URI
     * @param array $options Request options
     * @param string $token Authentication token (defaults to user token)
     * @return mixed Response
     */
    protected function apiRequest($method, $uri, $options = [], $token = null)
    {
        $useToken = $token ?: $this->authToken;
        
        if ($useToken) {
            $options['headers'] = array_merge(
                $options['headers'] ?? [],
                ['Authorization' => 'Bearer ' . $useToken]
            );
        }
        
        return $this->client->request($method, $uri, $options);
    }
    
    /**
     * Tear down the test environment
     */
    protected function tearDown(): void
    {
        $this->client = null;
        $this->db = null;
        parent::tearDown();
    }
}
