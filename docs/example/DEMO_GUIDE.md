# Factory Microservices Demo Guide

Complete step-by-step guide to demonstrate the factory microservices system.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Step 1: Initial Setup](#step-1-initial-setup)
- [Step 2: Start Services](#step-2-start-services)
- [Step 3: Database Setup](#step-3-database-setup)
- [Step 4: Verify Services](#step-4-verify-services)
- [Step 5: Demo Scenarios](#step-5-demo-scenarios)
- [Step 6: Monitoring & Observability](#step-6-monitoring--observability)
- [Troubleshooting](#troubleshooting)
- [Clean Up](#clean-up)

---

## Prerequisites

### Required Software
- Docker & Docker Compose
- JDK 21
- Git
- curl or Postman (for API testing)
- Web browser

### Check Prerequisites
```bash
# Check Docker
docker --version
docker-compose --version

# Check Java
java -version

# Check Git
git --version
```

---

## Step 1: Initial Setup

### 1.1 Clone and Navigate
```bash
cd /home/darksister/Documents/Project/devops/small-scale-factory-micro
```

### 1.2 Build the Project
```bash
# Clean and build all modules
./gradlew clean build

# This will:
# - Compile all Kotlin code
# - Run tests
# - Build fat JARs for each service
```

**Expected Output:**
```
BUILD SUCCESSFUL in 30s
```

### 1.3 Verify Build Artifacts
```bash
# Check that fat JARs were created
ls -lh auth-service/build/libs/auth-service-all.jar
ls -lh inventory-service/build/libs/inventory-service-all.jar
ls -lh order-service/build/libs/order-service-all.jar
```

---

## Step 2: Start Services

### 2.1 Start All Services with Docker Compose
```bash
docker-compose up --build
```

**This will start:**
- ✅ auth-db (PostgreSQL on port 5432)
- ✅ inventory-db (PostgreSQL on port 5433)
- ✅ order-db (PostgreSQL on port 5435)
- ✅ telegram-db (PostgreSQL on port 5434)
- ✅ auth-service (Ktor on port 8081)
- ✅ inventory-service (Ktor on port 8082)
- ✅ order-service (Ktor on port 8083)
- ✅ telegram-bot-service
- ✅ prometheus (port 9090)
- ✅ grafana (port 3000)

**Wait for health checks to pass** (approximately 1-2 minutes)

Look for:
```
auth_service     | [main] INFO  Application - Responding at http://0.0.0.0:8081
inventory_service | [main] INFO  Application - Responding at http://0.0.0.0:8082
order_service    | [main] INFO  Application - Responding at http://0.0.0.0:8083
```

### 2.2 Verify Containers are Running
Open a new terminal:
```bash
docker ps
```

You should see 11 containers running.

---

## Step 3: Database Setup

### 3.1 Run Migrations

#### Auth Service Migration
```bash
cd auth-service/src/main/resources/db
chmod +x migrate.sh
./migrate.sh 001__create_auth_schema.sql
```

**Expected Output:**
```
Running migration: 001__create_auth_schema.sql
✓ Migration completed successfully
```

#### Inventory Service Migration
```bash
cd ../../../../inventory-service/src/main/resources/db
chmod +x migrate.sh
./migrate.sh 002__create_inventory_schema.sql
```

#### Order Service Migration
```bash
cd ../../../../order-service/src/main/resources/db
chmod +x migrate.sh
./migrate.sh 003__create_order_schema.sql
```

### 3.2 Seed Demo Data

#### Seed Auth Service
```bash
cd ../../../auth-service/src/main/resources/db
chmod +x seed.sh
./seed.sh 001__seed_demo_users.sql
```

**Expected Output:**
```
Running seed: 001__seed_demo_users.sql
✓ Seed completed successfully
```

**This creates:**
- 3 roles (ADMIN, MANAGER, STAFF)
- 13 users (2 admins, 3 managers, 5 staff, 3 customers)
- All with password: `password123`

#### Seed Inventory Service
```bash
cd ../../../../inventory-service/src/main/resources/db
./seed.sh 001__seed_demo_inventory.sql
```

**This creates:**
- 3 branches (warehouses/stores)
- 5 product categories
- 15 inventory items
- Stock levels across branches
- Sample stock movements

#### Seed Order Service
```bash
cd ../../../../order-service/src/main/resources/db
./seed.sh 001__seed_demo_orders.sql
```

**This creates:**
- 7 orders with different statuses
- Multiple order items
- Order history spanning 25 days

---

## Step 4: Verify Services

### 4.1 Health Checks
```bash
# Auth Service
curl http://localhost:8081/health
# Expected: OK

# Inventory Service
curl http://localhost:8082/health
# Expected: OK

# Order Service
curl http://localhost:8083/health
# Expected: OK
```

### 4.2 Quick Database Check
```bash
# Check auth database
docker exec -it auth_postgres_db psql -U auth_user -d auth_db -c "SELECT COUNT(*) FROM users;"
# Expected: count > 0

# Check inventory database
docker exec -it inventory_postgres_db psql -U inventory_user -d inventory_db -c "SELECT COUNT(*) FROM inventory_items;"
# Expected: count > 0

# Check order database
docker exec -it order_postgres_db psql -U order_user -d order_db -c "SELECT COUNT(*) FROM orders;"
# Expected: count > 0
```

---

## Step 5: Demo Scenarios

### Scenario 1: User Authentication Flow

#### 5.1 Login as Admin
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "john.anderson@factory.com",
    "password": "password123"
  }'
```

**Save the `accessToken` from the response!**

Example response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "...",
    "user": {
      "id": "650e8400-e29b-41d4-a716-446655440001",
      "firstName": "John",
      "lastName": "Anderson",
      "email": "john.anderson@factory.com",
      "role": "ADMIN"
    }
  }
}
```

#### 5.2 Get User Profile
```bash
# Replace YOUR_TOKEN with the actual token
export ADMIN_TOKEN="your_access_token_here"

curl -X GET http://localhost:8081/v1/employee/auth/profile \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### 5.3 List All Users
```bash
curl -X GET "http://localhost:8081/v1/employee/auth/users?limit=20" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

### Scenario 2: Inventory Management

#### 5.4 View All Inventory Items
```bash
curl -X GET "http://localhost:8082/api/v1/inventory/items?limit=20" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**You should see 15 products including:**
- Dell XPS 13 Laptop
- Logitech Mouse
- Office supplies
- Raw materials
- Safety equipment

#### 5.5 Check Stock Levels at Main Warehouse
```bash
# Get branch ID for Main Warehouse: 750e8400-e29b-41d4-a716-446655440001
curl -X GET "http://localhost:8082/api/v1/inventory/stock-levels/branch/750e8400-e29b-41d4-a716-446655440001" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### 5.6 Check Low Stock Items
```bash
curl -X GET "http://localhost:8082/api/v1/inventory/stock-levels/low-stock" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### 5.7 Create New Inventory Item
```bash
curl -X POST http://localhost:8082/api/v1/inventory/items \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "TEST-PRODUCT-001",
    "name": "Test Product",
    "description": "A test product for demo",
    "unitOfMeasure": "piece",
    "unitPrice": 49.99,
    "reorderLevel": 10,
    "reorderQuantity": 50,
    "isActive": true
  }'
```

#### 5.8 Create Stock Movement
```bash
curl -X POST http://localhost:8082/api/v1/inventory/movements \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inventoryItemId": "950e8400-e29b-41d4-a716-446655440001",
    "toBranchId": "750e8400-e29b-41d4-a716-446655440001",
    "movementType": "IN",
    "quantity": 10,
    "unitPrice": 1299.99,
    "referenceNumber": "PO-DEMO-001",
    "notes": "Demo stock receipt"
  }'
```

---

### Scenario 3: Order Management (Customer)

#### 5.9 Login as Customer
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "alice.cooper@customer.com",
    "password": "password123"
  }'
```

**Save the customer token:**
```bash
export CUSTOMER_TOKEN="customer_access_token_here"
```

#### 5.10 View Customer Orders
```bash
curl -X GET http://localhost:8083/v1/customer/orders \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

**You should see Alice's existing orders**

#### 5.11 Create New Order
```bash
curl -X POST http://localhost:8083/v1/customer/orders \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "deliveryAddress": "123 Maple Street, Apt 4B, New York, NY 10001, USA",
    "notes": "Please deliver before 5 PM",
    "items": [
      {
        "productId": "950e8400-e29b-41d4-a716-446655440002",
        "productName": "Logitech MX Master 3 Mouse",
        "quantity": 2,
        "unitPrice": 99.99
      },
      {
        "productId": "950e8400-e29b-41d4-a716-446655440003",
        "productName": "Corsair K95 Mechanical Keyboard",
        "quantity": 1,
        "unitPrice": 199.99
      }
    ]
  }'
```

**Save the order ID from the response!**

#### 5.12 View Specific Order
```bash
# Replace ORDER_ID with actual ID from previous step
export ORDER_ID="your_order_id_here"

curl -X GET "http://localhost:8083/v1/customer/orders/$ORDER_ID" \
  -H "Authorization: Bearer $CUSTOMER_TOKEN"
```

---

### Scenario 4: Order Management (Employee)

#### 5.13 Login as Manager
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "michael.chen@factory.com",
    "password": "password123"
  }'
```

**Save the manager token:**
```bash
export MANAGER_TOKEN="manager_access_token_here"
```

#### 5.14 View All Orders
```bash
curl -X GET "http://localhost:8083/v1/employee/orders?limit=20" \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

**You should see all orders from all customers**

#### 5.15 Update Order Status
```bash
# Update the order we just created to PROCESSING
curl -X PUT "http://localhost:8083/v1/employee/orders/$ORDER_ID/status" \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PROCESSING"
  }'
```

#### 5.16 Verify Status Update
```bash
curl -X GET "http://localhost:8083/v1/employee/orders/$ORDER_ID" \
  -H "Authorization: Bearer $MANAGER_TOKEN"
```

**Status should now be "PROCESSING"**

---

### Scenario 5: Branch & Category Management

#### 5.17 Create New Branch
```bash
curl -X POST http://localhost:8082/api/v1/inventory/branches \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "West Coast Distribution",
    "code": "DC-WEST",
    "address": "456 Pacific Highway",
    "city": "San Francisco",
    "country": "USA",
    "phoneNumber": "+1-555-0199",
    "email": "dc.west@factory.com",
    "managerName": "Demo Manager",
    "isActive": true
  }'
```

#### 5.18 View All Branches
```bash
curl -X GET http://localhost:8082/api/v1/inventory/branches \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

**You should now see 4 branches**

#### 5.19 Create New Category
```bash
curl -X POST http://localhost:8082/api/v1/inventory/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Demo Category",
    "description": "A category created during demo",
    "isActive": true
  }'
```

---

## Step 6: Monitoring & Observability

### 6.1 Access Prometheus
Open in browser:
```
http://localhost:9090
```

**Try these queries:**
- `up` - Shows all services status
- `http_server_requests_seconds_count` - Request counts
- `jvm_memory_used_bytes` - Memory usage

### 6.2 Access Grafana
Open in browser:
```
http://localhost:3000
```

**Login:**
- Username: `admin`
- Password: `admin`

**Explore:**
- Pre-configured dashboards
- Service metrics
- Request rates
- Response times
- Error rates

### 6.3 View Service Metrics Directly
```bash
# Auth Service metrics
curl http://localhost:8081/metrics

# Inventory Service metrics
curl http://localhost:8082/metrics

# Order Service metrics
curl http://localhost:8083/metrics
```

---

## Demo Talking Points

### 1. **Microservices Architecture**
- ✅ Three independent services with separate databases
- ✅ Each service owns its data (auth-db, inventory-db, order-db)
- ✅ Service-to-service communication via REST APIs
- ✅ JWT-based authentication across services

### 2. **Database Independence**
- ✅ Each service has its own PostgreSQL instance
- ✅ No cross-database queries
- ✅ Data isolation and service autonomy
- ✅ Independent scaling capability

### 3. **Security & Authorization**
- ✅ Role-based access control (ADMIN, MANAGER, STAFF, CUSTOMER)
- ✅ JWT tokens with user_id and role claims
- ✅ Different authentication strategies for employees vs customers
- ✅ Token refresh mechanism

### 4. **API Design**
- ✅ RESTful API conventions
- ✅ Consistent response format across services
- ✅ Pagination support
- ✅ Comprehensive error handling

### 5. **Inventory Management**
- ✅ Multi-branch stock tracking
- ✅ Stock movement history
- ✅ Low-stock alerts
- ✅ Category hierarchy support

### 6. **Order Processing**
- ✅ Order lifecycle management (PENDING → PROCESSING → SHIPPED → DELIVERED)
- ✅ Customer order creation
- ✅ Employee order management
- ✅ Order history tracking

### 7. **Observability**
- ✅ Prometheus metrics collection
- ✅ Grafana dashboards
- ✅ Health check endpoints
- ✅ Structured logging

### 8. **DevOps & Deployment**
- ✅ Docker containerization
- ✅ Docker Compose orchestration
- ✅ Database migrations
- ✅ Seed data for testing/demo
- ✅ Separate build artifacts (fat JARs)

---

## Complete Demo Flow Script

### Quick 10-Minute Demo

```bash
# 1. Start everything
docker-compose up -d
# Wait 2 minutes

# 2. Run migrations & seed (in separate terminal)
cd auth-service/src/main/resources/db && ./migrate.sh 001__create_auth_schema.sql && ./seed.sh 001__seed_demo_users.sql
cd ../../../../inventory-service/src/main/resources/db && ./migrate.sh 002__create_inventory_schema.sql && ./seed.sh 001__seed_demo_inventory.sql
cd ../../../../order-service/src/main/resources/db && ./migrate.sh 003__create_order_schema.sql && ./seed.sh 001__seed_demo_orders.sql

# 3. Health checks
curl http://localhost:8081/health && curl http://localhost:8082/health && curl http://localhost:8083/health

# 4. Login as admin
curl -X POST http://localhost:8081/v1/auth/login -H "Content-Type: application/json" -d '{"identifier":"john.anderson@factory.com","password":"password123"}'
# Copy token

# 5. View inventory
export TOKEN="paste_token_here"
curl http://localhost:8082/api/v1/inventory/items -H "Authorization: Bearer $TOKEN"

# 6. Login as customer
curl -X POST http://localhost:8081/v1/auth/login -H "Content-Type: application/json" -d '{"identifier":"alice.cooper@customer.com","password":"password123"}'
# Copy customer token

# 7. View customer orders
export CUST_TOKEN="paste_customer_token_here"
curl http://localhost:8083/v1/customer/orders -H "Authorization: Bearer $CUST_TOKEN"

# 8. Open monitoring
# Browser: http://localhost:9090 (Prometheus)
# Browser: http://localhost:3000 (Grafana - admin/admin)
```

---

## Troubleshooting

### Services Not Starting
```bash
# Check container logs
docker-compose logs auth-service
docker-compose logs inventory-service
docker-compose logs order-service

# Check database logs
docker-compose logs auth-db
```

### Migration Fails
```bash
# Verify database is ready
docker exec -it auth_postgres_db psql -U auth_user -d auth_db -c "SELECT version();"

# Check if migration file exists
ls -la auth-service/src/main/resources/db/migration/
```

### Port Already in Use
```bash
# Find what's using the port
sudo lsof -i :8081
sudo lsof -i :5432

# Kill the process or change port in docker-compose.yml
```

### Authentication Fails
- Verify JWT_SECRET is set correctly
- Check token expiration
- Ensure user exists in database
- Verify password is correct (demo password: `password123`)

### Database Connection Issues
```bash
# Restart database containers
docker-compose restart auth-db inventory-db order-db

# Check database connectivity
docker exec -it auth_postgres_db psql -U auth_user -d auth_db -c "\dt"
```

---

## Clean Up

### Stop All Services
```bash
docker-compose down
```

### Remove All Data (Complete Reset)
```bash
# Stop and remove containers, networks, and volumes
docker-compose down -v

# Remove all volumes
docker volume rm auth_db_data inventory_db_data order_db_data telegram_db_data prometheus_data grafana_data

# Rebuild from scratch
docker-compose up --build
```

### Clean Build Artifacts
```bash
./gradlew clean
```

---

## Next Steps

After the demo, you can:

1. **Integrate Frontend**: Build React/Vue/Angular frontend using the API endpoints
2. **Add More Features**: Implement forgot password, email notifications, etc.
3. **Add Tests**: Write integration tests, load tests
4. **Deploy to Cloud**: Deploy to AWS, GCP, Azure using Kubernetes
5. **Add API Gateway**: Use Kong, NGINX, or API Gateway pattern
6. **Add Message Queue**: Integrate RabbitMQ/Kafka for async operations
7. **Add Caching**: Redis for frequently accessed data
8. **CI/CD Pipeline**: GitHub Actions, Jenkins, GitLab CI

---

## Demo Checklist

- [ ] All services are running
- [ ] All health checks pass
- [ ] Migrations completed successfully
- [ ] Seed data loaded
- [ ] Admin login works
- [ ] Customer login works
- [ ] Inventory endpoints accessible
- [ ] Order creation works
- [ ] Order status update works
- [ ] Prometheus accessible
- [ ] Grafana accessible
- [ ] All endpoints return correct responses

---

**Demo Duration:** 15-20 minutes for complete walkthrough
**Services:** 3 microservices + 4 databases + 2 monitoring tools
**Total Endpoints:** 46+ REST APIs
**Demo Users:** 13 users across 3 roles
**Sample Data:** 15 products, 3 branches, 7 orders

**Ready to impress!** 🚀
