# Quick Start - Factory Microservices

Fast setup guide to get the system running in under 5 minutes.

## Prerequisites
- Docker & Docker Compose installed
- Ports available: 8081, 8082, 8083, 5432-5435, 9090, 3000

---

## 🚀 Fast Setup (5 Steps)

### Step 1: Build
```bash
./gradlew clean build
```

### Step 2: Start Services
```bash
docker-compose up -d
```
Wait ~60 seconds for services to be healthy

### Step 3: Run Migrations
```bash
# Auth
cd auth-service/src/main/resources/db
./migrate.sh 001__create_auth_schema.sql

# Inventory
cd ../../../../inventory-service/src/main/resources/db
./migrate.sh 002__create_inventory_schema.sql

# Order
cd ../../../../order-service/src/main/resources/db
./migrate.sh 003__create_order_schema.sql
```

### Step 4: Seed Demo Data
```bash
# Auth
cd ../../../auth-service/src/main/resources/db
./seed.sh 001__seed_demo_users.sql

# Inventory
cd ../../../../inventory-service/src/main/resources/db
./seed.sh 001__seed_demo_inventory.sql

# Order
cd ../../../../order-service/src/main/resources/db
./seed.sh 001__seed_demo_orders.sql
```

### Step 5: Verify
```bash
curl http://localhost:8081/health  # Auth
curl http://localhost:8082/health  # Inventory
curl http://localhost:8083/health  # Order
```

---

## 🔑 Demo Credentials

| User | Email | Password | Role |
|------|-------|----------|------|
| Admin | `john.anderson@factory.com` | `Password123@` | ADMIN |
| Manager | `michael.chen@factory.com` | `Password123@` | MANAGER |
| Staff | `lisa.brown@factory.com` | `Password123@` | STAFF |
| Customer | `alice.cooper@customer.com` | `Password123@` | CUSTOMER |

---

## 🧪 Quick Test

### 1. Login
```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"john.anderson@factory.com","password":"Password123@"}'
```

Copy the `accessToken` from response.

### 2. View Inventory
```bash
export TOKEN="your_token_here"
curl http://localhost:8082/api/v1/inventory/items \
  -H "Authorization: Bearer $TOKEN"
```

### 3. View Orders
```bash
# Login as customer first
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"alice.cooper@customer.com","password":"Password123@"}'

# Use customer token
export CUST_TOKEN="customer_token_here"
curl http://localhost:8083/v1/customer/orders \
  -H "Authorization: Bearer $CUST_TOKEN"
```

---

## 📊 Monitoring

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

---

## 🗑️ Clean Up

```bash
# Stop everything
docker-compose down

# Remove all data
docker-compose down -v
```

---

## 📝 Service URLs

| Service | URL | Description |
|---------|-----|-------------|
| Auth | http://localhost:8081 | Authentication & user management |
| Inventory | http://localhost:8082 | Inventory & stock management |
| Order | http://localhost:8083 | Order management |
| Prometheus | http://localhost:9090 | Metrics |
| Grafana | http://localhost:3000 | Dashboards |

---

## 🐛 Troubleshooting

### Services not starting?
```bash
docker-compose logs auth-service
docker-compose logs inventory-service
docker-compose logs order-service
```

### Database issues?
```bash
docker-compose restart auth-db inventory-db order-db
```

### Port conflicts?
```bash
# Check what's using ports
sudo lsof -i :8081
sudo lsof -i :8082
sudo lsof -i :8083
```

---

## 📚 Full Documentation

- Complete API docs: `API_ENDPOINTS.md`
- Full demo guide: `DEMO_GUIDE.md`
- Project docs: `CLAUDE.md`

---

**Ready in 5 minutes!** ✅
