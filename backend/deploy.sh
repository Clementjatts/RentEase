#!/bin/bash

echo "======================================"
echo "RentEase Backend Deployment Script"
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
echo "2. Waiting for containers to start..."
sleep 5

# Initialize the database
echo "3. Initializing the database..."
docker-compose -f docker/docker-compose.yml exec web php /var/www/database/init_db.php

echo "4. Running tests..."
./test.sh

echo "======================================"
echo "RentEase Backend deployment completed!"
echo "API is now available at: http://localhost:8080"
echo "For Android Emulator use: http://10.0.2.2:8080"
echo "======================================"

echo "To stop the server, run: docker-compose -f docker/docker-compose.yml down"
