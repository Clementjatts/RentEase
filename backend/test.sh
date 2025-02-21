#!/bin/bash

echo "Testing RentEase API endpoints..."
echo "================================"

BASE_URL="http://localhost:8080"
ADMIN_AUTH="admin:pass"

echo "\n1. Testing hello endpoint..."
curl $BASE_URL/hello.php

echo "\n\n2. Testing landlord creation with authentication..."
LANDLORD_RESPONSE=$(curl -X POST -H "Content-Type: application/json" \
  -u "$ADMIN_AUTH" \
  -d '{"name":"Jane Smith","contact":"jane@example.com"}' \
  $BASE_URL/landlord/create.php)
echo $LANDLORD_RESPONSE

echo "\n\n3. Testing landlord read..."
curl -u "$ADMIN_AUTH" $BASE_URL/landlord/read.php

echo "\n\n4. Testing property creation with authentication..."
PROPERTY_RESPONSE=$(curl -X POST -H "Content-Type: application/json" \
  -u "$ADMIN_AUTH" \
  -d '{"title":"Luxury Villa","description":"A stunning 4-bedroom villa with pool","landlord_id":2}' \
  $BASE_URL/property/create.php)
echo $PROPERTY_RESPONSE

echo "\n\n5. Testing property read (all properties)..."
curl $BASE_URL/property/read.php

echo "\n\n6. Testing property update with authentication..."
curl -X PUT -H "Content-Type: application/json" \
  -u "$ADMIN_AUTH" \
  -d '{"id":2,"title":"Updated Luxury Villa","description":"A stunning 5-bedroom villa with pool and garden"}' \
  $BASE_URL/property/update.php

echo "\n\n7. Testing property read after update..."
curl $BASE_URL/property/read.php

echo "\n\n8. Testing user request creation..."
curl -X POST -H "Content-Type: application/json" \
  -d '{"user_id":"user123","property_id":2,"message":"Im interested in viewing this property"}' \
  $BASE_URL/request/create.php

echo "\n\n9. Testing user request read (all requests) with authentication..."
curl -u "$ADMIN_AUTH" $BASE_URL/request/read.php

echo "\n\n10. Testing user request read (by user)..."
curl "$BASE_URL/request/read.php?user_id=user123"

echo "\n\n11. Testing property delete with authentication..."
curl -X DELETE -H "Content-Type: application/json" \
  -u "$ADMIN_AUTH" \
  -d '{"id":2}' \
  $BASE_URL/property/delete.php

echo "\n\n12. Testing property read after delete..."
curl $BASE_URL/property/read.php

echo "\n\n13. Testing unauthorized access..."
curl -X POST -H "Content-Type: application/json" \
  -d '{"name":"Unauthorized","contact":"test@example.com"}' \
  $BASE_URL/landlord/create.php

echo "\n\nTests completed!"
