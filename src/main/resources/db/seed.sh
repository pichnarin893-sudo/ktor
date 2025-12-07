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
    echo "Example: ./seed.sh 001_mock_roles.sql"
    exit 1
fi

SEED_PATH="seeder/$SEED_FILE"

if [ ! -f "$SEED_PATH" ]; then
    echo -e "${RED}Error: Seeder file not found: $SEED_PATH${NC}"
    exit 1
fi

echo -e "${YELLOW}Running seeder: $SEED_FILE${NC}"

# Copy file to container and execute
sudo docker cp "$SEED_PATH" ktor-microservice_postgres_1:/tmp/seeder.sql
sudo docker-compose exec postgres psql -U postgres -d microservice_db -f /tmp/seeder.sql

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Seeder completed successfully${NC}"
    sudo docker-compose exec postgres rm /tmp/seeder.sql
else
    echo -e "${RED}✗ Seeder failed${NC}"
    exit 1
fi
