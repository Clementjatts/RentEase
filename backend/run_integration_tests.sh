#!/bin/bash

echo "Running RentEase API integration tests..."
docker-compose -f docker/docker-compose.yml exec api ./vendor/bin/phpunit tests/integration/AuthControllerTest.php
