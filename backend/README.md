# RentEase Backend API

This directory contains the backend API for the RentEase Android application.

## Structure

- `api/` - API endpoints and code
  - `auth/` - Authentication endpoints (login, register, user)
  - `landlord/` - Landlord management endpoints
  - `property/` - Property management endpoints
  - `request/` - User request management endpoints
  - `config/` - Configuration files
  - `utils/` - Utility functions
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
3. Run tests to verify the API is working

The API will be available at:
- http://localhost:8080 (from your computer)
- http://10.0.2.2:8080 (from Android Emulator)

### API Endpoints

#### Authentication

- `POST /auth/login` - Login with username and password
- `POST /auth/register` - Register a new user
- `GET /auth/user` - Get current user information
- `POST /auth/change-password` - Change user password

#### Landlords

- `POST /landlord/create.php` - Create a new landlord
- `GET /landlord/read.php` - Get all landlords
- `GET /landlord/read.php?id=X` - Get a specific landlord
- `PUT /landlord/update.php` - Update a landlord
- `DELETE /landlord/delete.php?id=X` - Delete a landlord

#### Properties

- `POST /property/create.php` - Create a new property
- `GET /property/read.php` - Get all properties
- `GET /property/read.php?id=X` - Get a specific property
- `PUT /property/update.php` - Update a property
- `DELETE /property/delete.php?id=X` - Delete a property
- `POST /property/upload_image.php` - Upload a property image

#### User Requests

- `POST /request/create.php` - Create a new user request
- `GET /request/read.php` - Get all user requests
- `GET /request/read.php?user_id=X` - Get user requests for a specific user

### Authentication

The API uses Basic Authentication:

```
Authorization: Basic base64(username:password)
```

For admin access:
- Username: admin
- Password: pass

## Development

To make changes to the database schema, modify the `database/schema.sql` file, then run `php database/init_db.php` to apply the changes.

To test the API locally without Docker, you can use PHP's built-in server:

```bash
cd api
php -S localhost:8080
```
