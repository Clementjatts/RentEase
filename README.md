# RentEase

A modern property rental management application that connects landlords and tenants, streamlining the rental process from property listing to lease management.

## Features

- **User Management**
  - Separate interfaces for landlords, tenants, and administrators
  - Secure authentication and authorization
  - Profile management and settings

- **Property Management**
  - Property listing and search
  - Image upload and management
  - Property details and specifications
  - Availability tracking

- **Request Management**
  - Property viewing requests
  - Application processing
  - Status tracking
  - Notification system

## Tech Stack

### Frontend (Android)
- Kotlin
- Android Jetpack Components
- MVVM Architecture
- Material Design
- Retrofit for API calls
- Room for local storage
- Coroutines for asynchronous operations

### Backend (PHP)
- RESTful API
- JWT Authentication
- MySQL Database
- PDO for database operations

## Setup

### Android App
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Update `local.properties` with your SDK path
5. Build and run the application

### Backend
1. Navigate to the `backend/api` directory
2. Copy `.env.example` to `.env` and configure your environment variables
3. Install dependencies using Composer:
   ```bash
   composer install
   ```
4. Set up the database using the schema in `backend/database/schema.sql`
5. Configure your web server to point to the `backend/api` directory

## Development

- Follow the MVVM architecture pattern
- Use Kotlin Coroutines for asynchronous operations
- Implement proper error handling
- Write unit tests for critical functionality
- Follow Material Design guidelines for UI

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is proprietary software. All rights reserved.

## Contact

For support or inquiries, please contact the development team. 