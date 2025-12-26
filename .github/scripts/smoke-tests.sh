#!/bin/bash

# Smoke Tests Script
# Usage: ./smoke-tests.sh <environment>
# Example: ./smoke-tests.sh staging

set -e

ENVIRONMENT=${1:-staging}
TIMEOUT=30
RETRY_COUNT=5

# Color codes for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Base URLs
if [ "$ENVIRONMENT" == "production" ]; then
    BASE_URL="https://factory.example.com"
    AUTH_URL="$BASE_URL:8081"
    INVENTORY_URL="$BASE_URL:8082"
    ORDER_URL="$BASE_URL:8083"
elif [ "$ENVIRONMENT" == "staging" ]; then
    BASE_URL="https://staging.factory.example.com"
    AUTH_URL="$BASE_URL:8081"
    INVENTORY_URL="$BASE_URL:8082"
    ORDER_URL="$BASE_URL:8083"
else
    echo -e "${RED}Invalid environment: $ENVIRONMENT${NC}"
    echo "Usage: $0 <staging|production>"
    exit 1
fi

echo -e "${YELLOW}Running smoke tests for $ENVIRONMENT environment...${NC}\n"

# Function to check health endpoint
check_health() {
    local service_name=$1
    local url=$2
    local retry=0

    echo -n "Checking $service_name health... "

    while [ $retry -lt $RETRY_COUNT ]; do
        if curl -sf -m $TIMEOUT "$url/health" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ OK${NC}"
            return 0
        fi
        retry=$((retry + 1))
        sleep 2
    done

    echo -e "${RED}✗ FAILED${NC}"
    return 1
}

# Function to test public endpoint
test_public_endpoint() {
    local service_name=$1
    local url=$2
    local expected_status=${3:-200}

    echo -n "Testing $service_name public endpoint... "

    status_code=$(curl -s -o /dev/null -w "%{http_code}" -m $TIMEOUT "$url")

    if [ "$status_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✓ OK (HTTP $status_code)${NC}"
        return 0
    else
        echo -e "${RED}✗ FAILED (HTTP $status_code, expected $expected_status)${NC}"
        return 1
    fi
}

# Function to test authentication
test_auth() {
    echo -n "Testing authentication... "

    # Try to login with demo credentials
    response=$(curl -s -m $TIMEOUT -X POST "$AUTH_URL/v1/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "identifier": "john.anderson@factory.com",
            "password": "Password123@"
        }')

    if echo "$response" | grep -q "token"; then
        echo -e "${GREEN}✓ OK${NC}"
        # Extract token for further tests
        export AUTH_TOKEN=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        return 0
    else
        echo -e "${RED}✗ FAILED${NC}"
        echo "Response: $response"
        return 1
    fi
}

# Function to test protected endpoint
test_protected_endpoint() {
    local service_name=$1
    local url=$2

    if [ -z "$AUTH_TOKEN" ]; then
        echo -e "${YELLOW}⚠ Skipping $service_name (no auth token)${NC}"
        return 0
    fi

    echo -n "Testing $service_name protected endpoint... "

    status_code=$(curl -s -o /dev/null -w "%{http_code}" -m $TIMEOUT "$url" \
        -H "Authorization: Bearer $AUTH_TOKEN")

    if [ "$status_code" -eq 200 ] || [ "$status_code" -eq 201 ]; then
        echo -e "${GREEN}✓ OK (HTTP $status_code)${NC}"
        return 0
    else
        echo -e "${RED}✗ FAILED (HTTP $status_code)${NC}"
        return 1
    fi
}

# Start smoke tests
echo "==================================="
echo "  Environment: $ENVIRONMENT"
echo "==================================="
echo ""

# Track failures
FAILED=0

# 1. Health Checks
echo "--- Health Checks ---"
check_health "Auth Service" "$AUTH_URL" || FAILED=$((FAILED + 1))
check_health "Inventory Service" "$INVENTORY_URL" || FAILED=$((FAILED + 1))
check_health "Order Service" "$ORDER_URL" || FAILED=$((FAILED + 1))
echo ""

# 2. Public Endpoints
echo "--- Public Endpoints ---"
test_public_endpoint "Inventory Categories" "$INVENTORY_URL/api/v1/inventory/categories" || FAILED=$((FAILED + 1))
test_public_endpoint "Inventory Items" "$INVENTORY_URL/api/v1/inventory/items" || FAILED=$((FAILED + 1))
echo ""

# 3. Authentication
echo "--- Authentication ---"
test_auth || FAILED=$((FAILED + 1))
echo ""

# 4. Protected Endpoints (if auth succeeded)
if [ -n "$AUTH_TOKEN" ]; then
    echo "--- Protected Endpoints ---"
    test_protected_endpoint "Employee Orders" "$ORDER_URL/v1/employee/orders" || FAILED=$((FAILED + 1))
    test_protected_endpoint "Customer Orders" "$ORDER_URL/v1/customer/orders" || FAILED=$((FAILED + 1))
    echo ""
fi

# 5. Database Connectivity (indirect test via API)
echo "--- Database Connectivity ---"
echo -n "Testing database via API... "
if curl -sf -m $TIMEOUT "$AUTH_URL/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"identifier":"test","password":"test"}' > /dev/null 2>&1; then
    # Even if credentials are wrong, this means DB is accessible
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
    FAILED=$((FAILED + 1))
fi
echo ""

# Summary
echo "==================================="
if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All smoke tests passed! ✓${NC}"
    exit 0
else
    echo -e "${RED}$FAILED smoke test(s) failed! ✗${NC}"
    exit 1
fi
