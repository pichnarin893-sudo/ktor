#!/bin/bash

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get migration file
MIGRATION_FILE=$1

if [ -z "$MIGRATION_FILE" ]; then
    echo -e "${RED}Usage: ./migrate.sh <migration_file>${NC}"
    echo "Example: ./migrate.sh 001_create_users_table.sql"
    exit 1
fi

MIGRATION_PATH="migrations/$MIGRATION_FILE"

if [ ! -f "$MIGRATION_PATH" ]; then
    echo -e "${RED}Error: Migration file not found: $MIGRATION_PATH${NC}"
    exit 1
fi

echo -e "${YELLOW}Running migration: $MIGRATION_FILE${NC}"

# Copy file to container and execute
sudo docker cp "$MIGRATION_PATH" ktor-microservice_postgres_1:/tmp/migration.sql
sudo docker-compose exec postgres psql -U postgres -d microservice_db -f /tmp/migration.sql

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Migration completed successfully${NC}"
    sudo docker-compose exec postgres rm /tmp/migration.sql
else
    echo -e "${RED}✗ Migration failed${NC}"
    exit 1
fi
