#!/bin/bash

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get rollback file
ROLLBACK_FILE=$1

if [ -z "$ROLLBACK_FILE" ]; then
    echo -e "${RED}Usage: ./rollback.sh <rollback_file>${NC}"
    echo "Example: ./rollback.sh 001_rollback_users_table.sql"
    exit 1
fi

ROLLBACK_PATH="rollbacks/$ROLLBACK_FILE"

if [ ! -f "$ROLLBACK_PATH" ]; then
    echo -e "${RED}Error: Rollback file not found: $ROLLBACK_PATH${NC}"
    exit 1
fi

echo -e "${YELLOW}Running rollback: $ROLLBACK_FILE${NC}"

# Copy file to container and execute
sudo docker cp "$ROLLBACK_PATH" ktor-microservice_postgres_1:/tmp/rollback.sql
sudo docker-compose exec postgres psql -U postgres -d microservice_db -f /tmp/rollback.sql

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Rollback completed successfully${NC}"
    sudo docker-compose exec postgres rm /tmp/rollback.sql
else
    echo -e "${RED}✗ Rollback failed${NC}"
    exit 1
fi
