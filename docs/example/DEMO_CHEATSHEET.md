# Demo Cheat Sheet - One Page Reference

## Setup Commands
```bash
# 1. Build
./gradlew clean build

# 2. Start
docker-compose up -d

# 3. Migrate & Seed (run all at once)
cd auth-service/src/main/resources/db && ./migrate.sh 001__create_auth_schema.sql && ./seed.sh 001__seed_demo_users.sql && \
cd ../../../../inventory-service/src/main/resources/db && ./migrate.sh 002__create_inventory_schema.sql && ./seed.sh 001__seed_demo_inventory.sql && \
cd ../../../../order-service/src/main/resources/db && ./migrate.sh 003__create_order_schema.sql && ./seed.sh 001__seed_demo_orders.sql && \
cd ../../../..
```

## Service URLs
| Service | URL |
|---------|-----|
| Auth | http://localhost:8081 |
| Inventory | http://localhost:8082 |
| Order | http://localhost:8083 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |

## Demo Accounts
| Email | Password | Role |
|-------|----------|------|
| john.anderson@factory.com | password123 | ADMIN |
| michael.chen@factory.com | password123 | MANAGER |
| alice.cooper@customer.com | password123 | CUSTOMER |

## Essential API Calls

### 1. Login (Admin)
```bash
curl -X POST http://localhost:8081/v1/auth/login -H "Content-Type: application/json" -d '{"identifier":"john.anderson@factory.com","password":"password123"}'
# Save token: export TOKEN="..."
```

### 2. View Inventory
```bash
curl http://localhost:8082/api/v1/inventory/items -H "Authorization: Bearer $TOKEN"
```

### 3. View Branches
```bash
curl http://localhost:8082/api/v1/inventory/branches -H "Authorization: Bearer $TOKEN"
```

### 4. Check Low Stock
```bash
curl http://localhost:8082/api/v1/inventory/stock-levels/low-stock -H "Authorization: Bearer $TOKEN"
```

### 5. Create Stock Movement
```bash
curl -X POST http://localhost:8082/api/v1/inventory/movements -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"inventoryItemId":"950e8400-e29b-41d4-a716-446655440001","toBranchId":"750e8400-e29b-41d4-a716-446655440001","movementType":"IN","quantity":10,"unitPrice":1299.99,"referenceNumber":"DEMO-001"}'
```

### 6. Login (Customer)
```bash
curl -X POST http://localhost:8081/v1/auth/login -H "Content-Type: application/json" -d '{"identifier":"alice.cooper@customer.com","password":"password123"}'
# Save: export CUST_TOKEN="..."
```

### 7. View Customer Orders
```bash
curl http://localhost:8083/v1/customer/orders -H "Authorization: Bearer $CUST_TOKEN"
```

### 8. Create Order
```bash
curl -X POST http://localhost:8083/v1/customer/orders -H "Authorization: Bearer $CUST_TOKEN" -H "Content-Type: application/json" -d '{"deliveryAddress":"123 Main St, NY","items":[{"productId":"950e8400-e29b-41d4-a716-446655440002","productName":"Logitech Mouse","quantity":2,"unitPrice":99.99}]}'
```

### 9. View All Orders (Manager)
```bash
# Login as manager first
curl -X POST http://localhost:8081/v1/auth/login -H "Content-Type: application/json" -d '{"identifier":"michael.chen@factory.com","password":"password123"}'
# Save: export MGR_TOKEN="..."

curl http://localhost:8083/v1/employee/orders -H "Authorization: Bearer $MGR_TOKEN"
```

### 10. Update Order Status
```bash
curl -X PUT http://localhost:8083/v1/employee/orders/ORDER_ID/status -H "Authorization: Bearer $MGR_TOKEN" -H "Content-Type: application/json" -d '{"status":"PROCESSING"}'
```

## Sample UUIDs (for testing)

### Products
- Dell Laptop: `950e8400-e29b-41d4-a716-446655440001`
- Logitech Mouse: `950e8400-e29b-41d4-a716-446655440002`
- Corsair Keyboard: `950e8400-e29b-41d4-a716-446655440003`

### Branches
- Main Warehouse: `750e8400-e29b-41d4-a716-446655440001`
- Downtown Store: `750e8400-e29b-41d4-a716-446655440002`

### Customers
- Alice Cooper: `650e8400-e29b-41d4-a716-446655440011`
- Bob Smith: `650e8400-e29b-41d4-a716-446655440012`

## Troubleshooting
```bash
# View logs
docker-compose logs -f auth-service
docker-compose logs -f inventory-service

# Restart services
docker-compose restart

# Complete reset
docker-compose down -v && docker-compose up -d
```

## Database Access
```bash
# Auth DB
docker exec -it auth_postgres_db psql -U auth_user -d auth_db

# Inventory DB
docker exec -it inventory_postgres_db psql -U inventory_user -d inventory_db

# Order DB
docker exec -it order_postgres_db psql -U order_user -d order_db
```

## Quick Stats
```bash
# Count users
docker exec -it auth_postgres_db psql -U auth_user -d auth_db -c "SELECT role_id, COUNT(*) FROM users GROUP BY role_id;"

# Count products
docker exec -it inventory_postgres_db psql -U inventory_user -d inventory_db -c "SELECT COUNT(*) FROM inventory_items;"

# Count orders by status
docker exec -it order_postgres_db psql -U order_user -d order_db -c "SELECT status, COUNT(*) FROM orders GROUP BY status;"
```

## Demo Flow (5 min)
1. ✅ Show health checks
2. ✅ Login as admin → get token
3. ✅ View inventory items (15 products)
4. ✅ Show low stock items
5. ✅ Login as customer → get customer token
6. ✅ Show customer's orders
7. ✅ Create new order
8. ✅ Login as manager
9. ✅ View all orders
10. ✅ Update order status
11. ✅ Open Grafana dashboard

## Cleanup
```bash
docker-compose down -v
```
