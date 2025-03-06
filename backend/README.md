# RentEase Backend API

This directory contains the backend API for the RentEase Android application.

## Structure

- `api/` - RESTful API implementation
  - `config/` - Configuration files
  - `controllers/` - API controllers
  - `models/` - Data models
  - `services/` - Service classes
  - `middleware/` - Authentication and validation middleware
  - `utils/` - Utility functions
  - `tests/` - Unit and integration tests
- `database/` - Database files and schema
- `docker/` - Docker configuration for deployment

## Getting Started

### Prerequisites

- Docker
- Docker Compose

### Deployment

Run the deployment script to start the backend:

```bash
./deploy.sh
```

This will:
1. Build and start the Docker containers
2. Initialize the database
3. Install dependencies
4. Run tests to verify the API is working

The API will be available at:
- http://localhost:8000 (from your computer)
- http://10.0.2.2:8000 (from Android Emulator)

### API Endpoints

#### Authentication

- `POST /auth/register` - Register a new user
- `POST /auth/login` - Login with email and password
- `GET /auth/me` - Get current user information

#### Properties

- `GET /properties` - Get all properties
- `GET /properties/{id}` - Get a specific property
- `POST /properties` - Create a new property
- `PUT /properties/{id}` - Update a property
- `DELETE /properties/{id}` - Delete a property

#### Requests

- `GET /requests` - Get all requests
- `GET /requests/{id}` - Get a specific request
- `GET /requests/user` - Get requests for the current user
- `POST /requests` - Create a new request
- `PUT /requests/{id}` - Update a request
- `DELETE /requests/{id}` - Delete a request

#### Landlords

- `GET /landlords` - Get all landlords
- `GET /landlords/{id}` - Get a specific landlord
- `POST /landlords` - Create a new landlord
- `PUT /landlords/{id}` - Update a landlord
- `DELETE /landlords/{id}` - Delete a landlord

### Authentication

The API uses JWT (JSON Web Token) authentication:

```
Authorization: Bearer {token}
```

The token is obtained by logging in with valid credentials.

## Development

### Running Tests

To run the automated tests:

```bash
./run_tests.sh
```

This will test all API endpoints and verify functionality.

### Database

The API can work with either SQLite or MySQL:

- SQLite is used for local development
- MySQL is used in the Docker environment

To make changes to the database schema, modify the appropriate files in `api/tests/fixtures/`.

### API Documentation

For detailed API documentation, refer to the integration tests in `api/tests/integration/` which demonstrate how to use each endpoint.
