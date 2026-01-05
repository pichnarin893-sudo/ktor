#!/bin/bash

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get seed file
SEED_FILE=$1

if [ -z "$SEED_FILE" ]; then
    echo -e "${RED}Usage: ./seed.sh <seed_file>${NC}"
    echo "Example: ./seed.sh 001__seed_demo_orders.sql"
    exit 1
fi

SEED_PATH="seed/$SEED_FILE"

if [ ! -f "$SEED_PATH" ]; then
    echo -e "${RED}Error: Seed file not found: $SEED_PATH${NC}"
    exit 1
fi

echo -e "${YELLOW}Running seed: $SEED_FILE${NC}"

# Copy file to container and execute
docker cp "$SEED_PATH" order_postgres_db:/tmp/seed.sql
docker exec -i order_postgres_db psql -U order_user -d order_db -f /tmp/seed.sql

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Seed completed successfully${NC}"
    docker exec order_postgres_db rm /tmp/seed.sql
else
    echo -e "${RED}✗ Seed failed${NC}"
    exit 1
fi
