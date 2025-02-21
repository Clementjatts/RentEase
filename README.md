# RentEase

A minimalist property advertisement application with mobile and web components.

## Project Structure

```
RentEase/
├── backend/
│   ├── api/
│   │   ├── config/
│   │   │   └── Database.php
│   │   ├── landlord/
│   │   │   ├── create.php
│   │   │   ├── read.php
│   │   │   ├── update.php
│   │   │   └── delete.php
│   │   └── hello.php
│   ├── database/
│   │   └── schema.sql
│   └── docker/
│       ├── Dockerfile
│       └── docker-compose.yml
└── README.md
```

## Sprint 1 Progress (Backend Setup)

### Completed Tasks:
- ✅ Created Docker environment for PHP and SQLite
- ✅ Implemented SQLite database schema
- ✅ Created basic API endpoints for landlord management
- ✅ Added test endpoint (hello.php)

### Setup Instructions

1. Install Docker and Docker Compose if not already installed
2. Navigate to the docker directory:
   ```bash
   cd backend/docker
   ```
3. Build and start the containers:
   ```bash
   docker-compose up --build
   ```
4. The API will be available at `http://localhost:8080`

### API Endpoints

- Test endpoint: `GET /hello.php`
- Landlord endpoints:
  - Create: `POST /landlord/create.php`
  - Read: `GET /landlord/read.php` or `GET /landlord/read.php?id=1`
  - Update: `PUT /landlord/update.php`
  - Delete: `DELETE /landlord/delete.php`

### Next Steps (Sprint 2)
- Set up Android Studio project with Kotlin
- Implement mobile UI components
- Connect mobile app to backend APIs
- Implement Room for local storage

## Project Timeline

- Sprint 1 (Backend): Feb 20 - Mar 5, 2025
- Sprint 2 (Mobile): Mar 6 - Mar 20, 2025
- Sprint 3 (Web, Testing, Docs): Mar 21 - Apr 4, 2025
