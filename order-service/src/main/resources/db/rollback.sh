#!/bin/bash

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Rolling back order database...${NC}"
echo -e "${RED}WARNING: This will drop all tables and data!${NC}"
read -p "Are you sure? (yes/no): " -r
echo

if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo "Rollback cancelled"
    exit 0
fi

# Create rollback SQL
cat > /tmp/rollback.sql << 'EOF'
-- Drop tables in correct order (respecting foreign keys)
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;

-- Drop custom types
DROP TYPE IF EXISTS order_status CASCADE;

-- Drop functions
DROP FUNCTION IF EXISTS update_orders_updated_at() CASCADE;
EOF

# Copy and execute
docker cp /tmp/rollback.sql order_postgres_db:/tmp/rollback.sql
docker exec -i order_postgres_db psql -U order_user -d order_db -f /tmp/rollback.sql

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Rollback completed successfully${NC}"
    docker exec order_postgres_db rm /tmp/rollback.sql
    rm /tmp/rollback.sql
else
    echo -e "${RED}✗ Rollback failed${NC}"
    rm /tmp/rollback.sql
    exit 1
fi
