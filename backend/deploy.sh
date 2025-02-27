#!/bin/bash

echo "======================================"
echo "RentEase API Deployment Script"
echo "======================================"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Error: Docker is not installed. Please install Docker before continuing."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "Error: Docker Compose is not installed. Please install Docker Compose before continuing."
    exit 1
fi

# Navigate to the backend directory
cd "$(dirname "$0")"

echo "1. Building and starting containers..."
docker-compose -f docker/docker-compose.yml up -d --build

# Wait for containers to start
echo "2. Waiting for database to initialize (this may take a minute)..."
sleep 20

# Check if database is ready
echo "3. Verifying database connection..."
MAX_RETRIES=10
RETRIES=0

while [ $RETRIES -lt $MAX_RETRIES ]; do
    if docker-compose -f docker/docker-compose.yml exec db mysqladmin ping -h db -u rentease -prentease_password --silent; then
        echo "Database connection successful!"
        break
    else
        echo "Waiting for database to be ready... (attempt $((RETRIES+1))/$MAX_RETRIES)"
        RETRIES=$((RETRIES+1))
        sleep 5
    fi
done

if [ $RETRIES -eq $MAX_RETRIES ]; then
    echo "Error: Failed to connect to database after $MAX_RETRIES attempts"
    echo "Check logs: docker-compose -f docker/docker-compose.yml logs db"
    exit 1
fi

echo "4. Installing Composer dependencies..."
docker-compose -f docker/docker-compose.yml exec api composer install

echo "5. Regenerating the autoloader..."
docker-compose -f docker/docker-compose.yml exec api composer dump-autoload -o

echo "6. Running database setup..."
docker-compose -f docker/docker-compose.yml exec api php -r "require_once 'tests/bootstrap.php'; setupTestDatabase();"

echo "7. Running unit tests..."
docker-compose -f docker/docker-compose.yml exec api ./vendor/bin/phpunit tests/unit

echo "======================================"
echo "RentEase API deployment completed!"
echo "API is now available at: http://localhost:8000"
echo "For Android Emulator use: http://10.0.2.2:8000"
echo "======================================"

echo "To stop the server, run: docker-compose -f docker/docker-compose.yml down"
