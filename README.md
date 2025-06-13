# RentEase

A property rental management application built for Android with a PHP backend API. This is an academic project demonstrating modern mobile app development with RESTful API integration.

## Project Structure

```
RentEase/
├── app/                    # Android application (Kotlin)
├── backend/               # Backend API and services
│   ├── api/              # PHP REST API
│   ├── web/              # Web interface
│   ├── database/         # SQLite database and schema
│   └── uploads/          # File uploads directory
├── api/                  # API tests
└── gradle/               # Gradle configuration
```

## Setup Instructions

### Prerequisites
- Android Studio (latest version)
- Docker and Docker Compose
- Android device or emulator
- ADB (Android Debug Bridge)

### Backend Setup

1. **Start the backend services**:
   ```bash
   cd backend
   docker-compose up -d
   ```

2. **Initialize the database**:
   The database will be automatically created from `backend/database/schema.sql` on first run.

3. **Verify backend is running**:
   Visit `http://localhost:8000` in your browser.

### Android App Setup

1. **Open the project in Android Studio**

2. **Set up port forwarding for emulator**:
   ```bash
   adb reverse tcp:8000 tcp:8000
   ```
   This forwards the emulator's localhost:8000 to your computer's localhost:8000 where the backend runs.

3. **Build and run the app**:
   - Connect your Android device via WiFi debugging or use an emulator
   - Click "Run" in Android Studio

### Default Credentials

- **Admin**: username: `admin`, password: `password`
- **Test Landlords**: username: `oluwaseun`, password: `123456`

## API Endpoints

The backend provides RESTful endpoints for:
- `/auth/*` - Authentication (login, register)
- `/users/*` - User management
- `/properties/*` - Property CRUD operations
- `/requests/*` - Tenant request management

Full API documentation is available by examining `backend/api/config/Routes.php`.

## Development Notes

### Port Forwarding
The Android app connects to the backend using `localhost:8000`. When using an Android emulator, you must set up ADB reverse port forwarding as shown above. This maps the emulator's localhost to your development machine's localhost.

### Database Location
The SQLite database is stored at `backend/database/rentease.db` on your local machine, not inside the Docker container. This ensures data persistence across container restarts.

### File Uploads
Property images are stored in `backend/uploads/properties/` and served via the web server.

## Troubleshooting

### Common Issues

1. **App can't connect to backend**:
   - Ensure Docker containers are running: `docker-compose ps`
   - Verify port forwarding: `adb reverse tcp:8000 tcp:8000`
   - Check backend logs: `docker-compose logs api`

2. **Database connection errors**:
   - Ensure the `backend/database/` directory exists and is writable
   - Check Docker volume mounts in `docker-compose.yml`

3. **Image uploads not working**:
   - Verify `backend/uploads/properties/` directory exists and has proper permissions
   - Check Docker volume configuration

### Logs
- **Backend logs**: `docker-compose logs api`
- **Android logs**: Use Android Studio's Logcat or `adb logcat`

## License

This is an academic project for educational purposes.
