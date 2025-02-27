#!/bin/bash

echo "Testing RentEase API endpoints..."
echo "================================"

BASE_URL="http://localhost:8000"
TOKEN=""

echo -e "\n1. Testing API health check..."
curl -s $BASE_URL

echo -e "\n\n2. Testing authentication endpoints..."
echo -e "\n2.1 Register a new user..."
REGISTER_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123","password_confirm":"password123"}' \
  $BASE_URL/auth/register)
echo $REGISTER_RESPONSE

echo -e "\n\n2.2 Login with the new user..."
LOGIN_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}' \
  $BASE_URL/auth/login)
echo $LOGIN_RESPONSE

# Extract token from login response
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')
echo -e "\n\nToken: $TOKEN"

echo -e "\n\n2.3 Get current user..."
curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/auth/me

echo -e "\n\n3. Testing property endpoints..."
echo -e "\n3.1 Create a new property..."
PROPERTY_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Luxury Apartment","description":"A beautiful 2-bedroom apartment","price":"1500","location":"Downtown","bedrooms":2,"bathrooms":2,"size":1200}' \
  $BASE_URL/properties)
echo $PROPERTY_RESPONSE

# Extract property ID from response
PROPERTY_ID=$(echo $PROPERTY_RESPONSE | grep -o '"id":[0-9]*' | sed 's/"id"://')
echo -e "\n\nProperty ID: $PROPERTY_ID"

echo -e "\n\n3.2 Get all properties..."
curl -s $BASE_URL/properties

echo -e "\n\n3.3 Get property by ID..."
curl -s $BASE_URL/properties/$PROPERTY_ID

echo -e "\n\n3.4 Update property..."
curl -s -X PUT -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Updated Luxury Apartment","description":"A beautiful 3-bedroom apartment","price":"1800","location":"Downtown","bedrooms":3,"bathrooms":2,"size":1500}' \
  $BASE_URL/properties/$PROPERTY_ID

echo -e "\n\n3.5 Get property after update..."
curl -s $BASE_URL/properties/$PROPERTY_ID

echo -e "\n\n4. Testing favorites endpoints..."
echo -e "\n4.1 Add property to favorites..."
FAVORITE_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"property_id\":$PROPERTY_ID}" \
  $BASE_URL/favorites)
echo $FAVORITE_RESPONSE

echo -e "\n\n4.2 Get user favorites..."
curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/favorites/user

echo -e "\n\n5. Testing request endpoints..."
echo -e "\n5.1 Create a new request..."
REQUEST_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"property_id\":$PROPERTY_ID,\"message\":\"I'm interested in viewing this property\",\"contact_method\":\"email\"}" \
  $BASE_URL/requests)
echo $REQUEST_RESPONSE

# Extract request ID from response
REQUEST_ID=$(echo $REQUEST_RESPONSE | grep -o '"id":[0-9]*' | sed 's/"id"://')
echo -e "\n\nRequest ID: $REQUEST_ID"

echo -e "\n\n5.2 Get user requests..."
curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/requests/user

echo -e "\n\n5.3 Get request by ID..."
curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/requests/$REQUEST_ID

echo -e "\n\n6. Testing cleanup..."
echo -e "\n6.1 Delete request..."
curl -s -X DELETE -H "Authorization: Bearer $TOKEN" $BASE_URL/requests/$REQUEST_ID

echo -e "\n\n6.2 Delete favorite..."
curl -s -X DELETE -H "Authorization: Bearer $TOKEN" $BASE_URL/favorites?property_id=$PROPERTY_ID

echo -e "\n\n6.3 Delete property..."
curl -s -X DELETE -H "Authorization: Bearer $TOKEN" $BASE_URL/properties/$PROPERTY_ID

echo -e "\n\n==================================="
echo "API tests completed. Check responses above for any errors."
echo "==================================="
