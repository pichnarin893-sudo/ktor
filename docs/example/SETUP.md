# NatJoub Microservices - Setup Guide

## 🚀 Quick Start

### Option 1: Docker Compose (Recommended)

```bash
# 1. Start all services
docker-compose up -d

# 2. Wait for services to be ready (check health)
docker-compose ps

# 3. Run database migrations
cd ktor/auth-service/src/main/resources/db$ ./migrate.sh 001_create_auth_schema.sql
cd ktor/inventory-service/src/main/resources/db$ ./migrate.sh 002_create_inventory_schema.sql


# 4. (Optional) Load seed data
cd ktor/auth-service/src/main/resources/db$ ./seed.sh 001_mock_roles.sql
cd ktor/auth-service/src/main/resources/db$ ./seed.sh 002_mock-users.sql
cd ktor/auth-service/src/main/resources/db$ ./seed.sh 003-mock_credentials.sql
cd ktor/inventory-service/src/main/resources/db$ ./seed.sh 004__mock_branches.sql
cd ktor/inventory-service/src/main/resources/db$ ./seed.sh 005__mock_categories.sql
cd ktor/inventory-service/src/main/resources/db$ ./seed.sh 006__mock_inventory_items.sql
cd ktor/inventory-service/src/main/resources/db$ ./seed.sh 007__mock_stock_levels.sql

#4.1 (Optional) Rollback
cd ktor/auth-service/src/main/resources/db$ ./rollback.sh 001_create_auth_schema.sql
cd ktor/inventory-service/src/main/resources/db$ ./rollback.sh 002_drop_inventory_schema.sql

# 5. Verify everything is running
curl http://localhost:8080/health
```

### Option 2: Local Development

```bash
# 1. Start PostgreSQL
docker run -d \
  --name natjoub-postgres \
  -e POSTGRES_DB=microservice_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# 2. Initialize schemas
psql -U postgres -d microservice_db -c "CREATE SCHEMA IF NOT EXISTS auth_schema;"
psql -U postgres -d microservice_db -c "CREATE SCHEMA IF NOT EXISTS inventory_schema;"

# 3. Run migrations
psql -U postgres -d microservice_db < src/main/resources/db/migration/001__create_auth_schema.sql
psql -U postgres -d microservice_db < src/main/resources/db/migration/002__create_inventory_schema.sql

# 4. (Optional) Load seed data
psql -U postgres -d microservice_db < src/main/resources/db/seeder/001__mock_roles.sql
psql -U postgres -d microservice_db < src/main/resources/db/seeder/002__mock_users.sql
psql -U postgres -d microservice_db < src/main/resources/db/seeder/003__mock_credentials.sql
# ... and other seeders

# 5. Build and run application
./gradlew build -x test
./gradlew run
```

## 🧪 Testing the Services

### 1. Test Auth Service

```bash
# Login with seeded admin user
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "admin@example.com",
    "password": "admin123"
  }' | jq -r '.data.accessToken')

echo "Token: $TOKEN"

# Get user profile
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/admin/auth/profile | jq
```

### 2. Test Inventory Service

```bash
# List all branches
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/inventory/branches | jq

# List all inventory items
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/inventory/items | jq

# Check low stock items
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/inventory/stock-levels/low-stock | jq
```

## 📊 Monitoring

- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/health
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

## 🛠️ Common Commands

```bash
# View logs
docker-compose logs -f app

# Restart services
docker-compose restart

# Stop services
docker-compose down

# Reset everything (including data)
docker-compose down -v

# Check database
docker exec -it ktor-microservice_postgres_1 psql -U postgres -d microservice_db

# Inside psql:
# \dn                          # List schemas
# \dt auth_schema.*            # List auth tables
# \dt inventory_schema.*       # List inventory tables
# SELECT * FROM auth_schema.users;
```

## ⚠️ Troubleshooting

### Tables not created?

Run migrations manually:
```bash
docker exec ktor-microservice_postgres_1 psql -U postgres -d microservice_db < src/main/resources/db/migration/001__create_auth_schema.sql
docker exec ktor-microservice_postgres_1 psql -U postgres -d microservice_db < src/main/resources/db/migration/002__create_inventory_schema.sql
```

### Can't login?

Load seed data:
```bash
docker exec ktor-microservice_postgres_1 psql -U postgres -d microservice_db < src/main/resources/db/seeder/001__mock_roles.sql
docker exec ktor-microservice_postgres_1 psql -U postgres -d microservice_db < src/main/resources/db/seeder/002__mock_users.sql
docker exec ktor-microservice_postgres_1 psql -U postgres -d microservice_db < src/main/resources/db/seeder/003__mock_credentials.sql
```

### Permission denied errors?

```bash
chmod 644 init-schemas.sql
chmod 644 src/main/resources/db/migration/*.sql
chmod 644 src/main/resources/db/seeder/*.sql
chmod 644 src/main/resources/db/rollback/*.sql

```

## 🎯 Default Test Credentials

After loading seed data:

- **Admin**: admin@example.com / admin@123
- **Manager**: manager@example.com / manager@123
- **Staff**: staff@example.com / staff@123
