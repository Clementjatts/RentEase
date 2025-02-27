# RentEase API Testing Framework

This directory contains a comprehensive testing framework for the RentEase API, including both unit tests and integration tests to verify the functionality, security, and error handling of all API endpoints.

## Test Structure

- **Unit Tests**: Located in `/tests/unit`, these tests verify individual components in isolation.
- **Integration Tests**: Located in `/tests/integration`, these tests verify the interaction between components and the overall functionality of the API.
- **Fixtures**: Located in `/tests/fixtures`, these files contain the database schema and test data used in the tests.

## Test Coverage

The testing framework covers:

1. **Authentication and Authorization**
   - User registration and login
   - JWT token handling
   - Role-based access control

2. **CRUD Operations**
   - User management
   - Property listings
   - Favorites
   - Viewing requests
   - Landlord management

3. **Error Handling**
   - Invalid requests
   - Missing or invalid authentication
   - Proper HTTP status codes
   - Consistent error responses

4. **Security**
   - Input validation
   - SQL injection protection
   - XSS protection
   - JWT token validation

## Running the Tests

### Prerequisites

- PHP 7.4+
- Composer
- MySQL/MariaDB

### Setup

1. Install the required dependencies:

```bash
cd /path/to/rentease/backend/api_new
composer install
```

2. Set up a test database:

```bash
mysql -u root -p -e "CREATE DATABASE rentease_test;"
```

3. Update the environment variables in `phpunit.xml` if needed:

```xml
<php>
    <env name="APP_ENV" value="testing"/>
    <env name="DB_HOST" value="localhost"/>
    <env name="DB_NAME" value="rentease_test"/>
    <env name="DB_USER" value="root"/>
    <env name="DB_PASS" value=""/>
    <env name="API_URL" value="http://localhost/backend/api_new"/>
</php>
```

### Running All Tests

```bash
composer test
```

### Running Specific Test Suites

- Unit tests only:

```bash
composer test-unit
```

- Integration tests only:

```bash
composer test-integration
```

### Running Individual Test Files

```bash
./vendor/bin/phpunit tests/unit/JWTTest.php
./vendor/bin/phpunit tests/integration/AuthControllerTest.php
```

## Test Reports

After running the tests, a coverage report will be generated in `tests/coverage`. Open `tests/coverage/index.html` in a browser to view the report.

## Continuous Integration

These tests are designed to be run in a CI/CD environment. Make sure your CI pipeline executes these tests before deploying any changes to production.

## Extending the Tests

When adding new features to the API, follow these guidelines:

1. Add unit tests for any new classes or methods
2. Add integration tests for any new endpoints
3. Update existing tests if you modify existing functionality
4. Run the full test suite before committing changes

## Common Issues

- **Database connection issues**: Make sure the database credentials in `phpunit.xml` are correct
- **Missing dependencies**: Run `composer install` to install all required packages
- **Permission issues**: Make sure the test directory has proper file permissions
- **Timeout issues**: For long-running tests, adjust the timeout settings in PHPUnit configuration
