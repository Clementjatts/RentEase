#!/bin/bash

# Start the PHP development server for testing
echo "Starting RentEase development server on http://localhost:8000"
echo "Press Ctrl+C to stop the server"
cd "$(dirname "$0")/api"
php -S localhost:8000
