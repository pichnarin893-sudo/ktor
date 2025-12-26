# Factory Management Microservices System

A production-ready microservices architecture for factory management with multi-channel customer access, built with Kotlin, Ktor, and PostgreSQL. This system demonstrates modern DevOps practices including containerization, observability, database-per-service pattern, and omnichannel customer experience.

## Table of Contents
- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Services](#services)
- [Multi-Channel Access](#multi-channel-access)
- [API Documentation](#api-documentation)
- [Database Architecture](#database-architecture)
- [Authentication & Authorization](#authentication--authorization)
- [Observability](#observability)
- [Demo Credentials](#demo-credentials)
- [Troubleshooting](#troubleshooting)

---

## Project Overview

### Domain
Factory management system with omnichannel customer access - customers can browse and order via Telegram bot, while employees use REST API for operations.

### Purpose
Provide a scalable, maintainable microservices platform demonstrating:
- ✅ Service isolation and independence
- ✅ Database-per-service pattern
- ✅ Role-based access control (RBAC)
- ✅ Multi-channel architecture (REST API + Telegram Bot)
- ✅ Public browsing + authenticated transactions
- ✅ Inter-service communication
- ✅ Production-ready observability
- ✅ Container orchestration with Docker Compose

### Target Users
- **Customers**: Browse products and place orders via **Telegram Bot** (role: customer)
- **Factory Employees**: Manage inventory, process orders via **REST API** (role: employee)
- **System Administrators**: Monitor system health, manage users

---

## Architecture

### Multi-Channel Microservices Design

```
┌──────────────────────────────────────────────────────────────────┐
│                        CLIENT CHANNELS                            │
├─────────────────────────┬────────────────────────────────────────┤
│   📱 Telegram Bot       │   💻 REST API (Postman/curl)          │
│   (Customers)           │   (Employees)                          │
└───────────┬─────────────┴────────────────┬───────────────────────┘
            │                              │
            ▼                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    MICROSERVICES LAYER                           │
├──────────────────┬──────────────────┬──────────────────────────┤
│  Auth Service    │ Inventory Service│   Order Service          │
│  Port: 8081      │  Port: 8082      │   Port: 8083             │
│                  │                  │                          │
│  • Login         │  • Categories📂  │   • Create Orders        │
│  • Register      │  • Products 📦   │   • View Orders          │
│  • JWT Tokens    │  • Stock Mgmt    │   • Update Status        │
│  • User Mgmt     │  • Branches      │                          │
└────────┬─────────┴────────┬─────────┴────────┬─────────────────┘
         │                  │                  │
         ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                                │
├──────────────────┬──────────────────┬──────────────────────────┤
│  auth_db         │  inventory_db    │   order_db               │
│  Port: 5432      │  Port: 5433      │   Port: 5435             │
└──────────────────┴──────────────────┴──────────────────────────┘
         │                  │                  │
         └──────────────────┴──────────────────┘
                            │
         ┌──────────────────┴──────────────────┐
         ▼                                     ▼
┌──────────────────┐              ┌──────────────────┐
│   Prometheus     │              │     Grafana      │
│   Port: 9090     │◀─────────────│   Port: 3000     │
└──────────────────┘              └──────────────────┘
    (Metrics)                        (Dashboards)
```

### Service Communication Flow

**Customer Journey (Telegram):**
```
Customer → /categories → Inventory Service (Public API)
Customer → /products → Inventory Service (Public API)
Customer → /order → Bot Auto-Login → Auth Service → Order Service (Protected)
```

**Employee Journey (REST API):**
```
Employee → Login → Auth Service → JWT Token
Employee → Manage Inventory → Inventory Service (Protected)
Employee → Process Orders → Order Service (Protected)
```

---

## Technology Stack

### Backend
- **Framework**: Ktor 2.3.7 (Kotlin JVM)
- **Language**: Kotlin 1.9.22 (JVM 21)
- **ORM**: Exposed 0.46.0
- **Database**: PostgreSQL 16 (Alpine)
- **DI**: Koin 3.5.3
- **Authentication**: JWT (io.ktor:ktor-server-auth-jwt)
- **Serialization**: Kotlinx Serialization

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Monitoring**: Prometheus 3.8.1
- **Visualization**: Grafana Latest
- **Build Tool**: Gradle 8.5 (Kotlin DSL)

### Telegram Integration
- **Bot API**: Telegram Bot API (Long Polling)
- **HTTP Client**: Ktor Client (CIO Engine)

---

## Getting Started

### Prerequisites
```bash
- Docker & Docker Compose
- JDK 21 (for local development)
- Gradle 8.5+ (included via wrapper)
- Telegram account (for bot testing)
```

### Quick Start (5 Minutes)

1. **Clone the repository**
```bash
git clone <repository-url>
cd small-scale-factory-micro
```

2. **Start all services**
```bash
docker-compose up --build -d
```

3. **Verify services are running**
```bash
docker-compose ps
```

Expected output:
```
NAME                    STATUS
auth_service            Up (healthy)
inventory_service       Up (healthy)
order_service           Up (healthy)
telegram_bot_service    Up
auth_postgres_db        Up (healthy)
inventory_postgres_db   Up (healthy)
order_postgres_db       Up (healthy)
factory_prometheus      Up
factory_grafana         Up
```

4. **Access services**
- Auth API: http://localhost:8081
- Inventory API: http://localhost:8082
- Order API: http://localhost:8083
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

5. **Test Telegram Bot**
- Open Telegram app
- Search for your bot
- Send `/start` to register
- Send `/categories` to browse products

---

## Services

### 1. Auth Service (Port 8081)
**Purpose**: User authentication, authorization, and JWT token management

**Features**:
- User registration (employee/customer)
- Login with JWT token generation
- Telegram customer auto-registration
- Role-based access control
- Token refresh mechanism

**Database**: `auth_db` (PostgreSQL 5432)
- Tables: users, credentials, roles, refresh_tokens, token_blacklist

### 2. Inventory Service (Port 8082)
**Purpose**: Product catalog, inventory, and stock management

**Features**:
- **Public Access** (No Auth):
  - Browse categories
  - View products
  - Filter products by category
- **Protected Access** (Auth Required):
  - Create/update/delete products
  - Manage categories
  - Stock level management
  - Branch management
  - Stock movement tracking

**Database**: `inventory_db` (PostgreSQL 5433)
- Tables: categories, inventory_items, stock_levels, stock_movements, branches

### 3. Order Service (Port 8083)
**Purpose**: Order processing and management

**Features**:
- **Customer Endpoints**:
  - Create orders (requires auth)
  - View own orders (requires auth)
  - Order history
- **Employee Endpoints**:
  - View all orders
  - Update order status
  - Order analytics

**Database**: `order_db` (PostgreSQL 5435)
- Tables: orders, order_items, order_status_history

### 4. Telegram Bot Service
**Purpose**: Customer-facing chat interface for browsing and ordering

**Features**:
- `/start` - Register as customer
- `/categories` - Browse all categories
- `/category <id>` - View products by category
- `/products` - Browse all products
- `/order <product_id> <qty>` - Place order
- `/myorders` - View order history
- `/help` - Show available commands

**Key Characteristics**:
- **Stateless**: No database, all data fetched from other services
- **Auto-authentication**: Automatically logs in customers using Telegram ID
- **Real-time**: Long polling for instant message processing

---

## Multi-Channel Access

### Channel Comparison

| Feature | Telegram Bot (Customers) | REST API (Employees) |
|---------|-------------------------|---------------------|
| **Authentication** | Auto (via Telegram ID) | Manual (JWT) |
| **Browse Products** | ✅ Public | ✅ Public |
| **Place Orders** | ✅ Auto-login | ✅ Manual login |
| **Manage Inventory** | ❌ Not available | ✅ Full access |
| **Process Orders** | ❌ Not available | ✅ Full access |
| **User Experience** | Chat-based, conversational | API-based, programmatic |

### Architecture Benefits
- **Customer Convenience**: Order via familiar Telegram interface
- **Employee Control**: Full management via REST API
- **Service Reusability**: Same backend serves both channels
- **Scalability**: Each channel scales independently
- **Security**: Public browsing, protected transactions

---

## API Documentation

See [API_ENDPOINTS.md](./API_ENDPOINTS.md) for comprehensive API documentation.

### Quick Examples

**1. Public Product Browsing (No Auth)**
```bash
# Browse categories
curl http://localhost:8082/api/v1/inventory/categories

# View all products
curl http://localhost:8082/api/v1/inventory/items?limit=10

# Filter by category
curl http://localhost:8082/api/v1/inventory/items?categoryId=<category-id>
```

**2. Employee Login & Order Management**
```bash
# Login
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "john.anderson@factory.com",
    "password": "Password123@"
  }'

# Use token to view orders
export TOKEN="<your-jwt-token>"
curl http://localhost:8083/v1/employee/orders \
  -H "Authorization: Bearer $TOKEN"
```

**3. Customer Order via Telegram**
```
Customer: /categories
Bot: Shows all categories

Customer: /category <id>
Bot: Shows products in that category

Customer: /order <product-id> 1
Bot: Auto-login → Creates order → Confirmation
```

---

## Database Architecture

### Database-Per-Service Pattern

Each service has its own PostgreSQL database for complete isolation:

```
┌─────────────────────────────────────────────────────────┐
│                   PostgreSQL Instances                   │
├──────────────────┬──────────────────┬───────────────────┤
│   auth_db        │  inventory_db    │   order_db        │
│   Port: 5432     │  Port: 5433      │   Port: 5435      │
│                  │                  │                   │
│  • users         │  • categories    │   • orders        │
│  • credentials   │  • items         │   • order_items   │
│  • roles         │  • stock_levels  │   • status_log    │
│  • tokens        │  • movements     │                   │
└──────────────────┴──────────────────┴───────────────────┘
```

### Schema Management

**Migrations**:
- Located in each service's `src/main/resources/db/migration/`
- Naming: `001__description.sql`, `002__description.sql`
- Applied via migration scripts

**Seeders**:
- Located in `src/main/resources/db/seeder/`
- Provides demo data for testing
- Password: `Password123@` (BCrypt hashed)

---

## Authentication & Authorization

### Public vs Protected Endpoints

#### 🔓 Public Endpoints (No Authentication)
- `GET /api/v1/inventory/categories`
- `GET /api/v1/inventory/categories/{id}`
- `GET /api/v1/inventory/items`
- `GET /api/v1/inventory/items?categoryId=<id>`
- `GET /api/v1/inventory/items/{id}`
- `POST /v1/auth/register`
- `POST /v1/auth/login`

#### 🔐 Protected Endpoints (JWT Required)

**Customer Role:**
- `POST /v1/customer/orders` - Create order
- `GET /v1/customer/orders` - View own orders

**Employee Role:**
- All inventory management (POST, PUT, DELETE)
- `GET /v1/employee/orders` - View all orders
- `PUT /v1/employee/orders/{id}/status` - Update order status

### JWT Token Structure
```json
{
  "sub": "user_id",
  "role": "employee",
  "exp": 1640000000
}
```

### Telegram Bot Authentication
```kotlin
// Auto-registration on /start
email: "customer_<telegram_id>@telegram.bot"
password: "telegram_<telegram_id>"

// Auto-login before orders
token = authClient.loginCustomer(telegramId)
orderClient.createOrder(token, ...)
```

---

## Observability

### Prometheus Metrics (Port 9090)

**Targets**:
- `auth_service:8081/metrics`
- `inventory_service:8082/metrics`
- `order_service:8083/metrics`

**Key Metrics**:
- HTTP request duration
- Request counts by endpoint
- Error rates
- JVM metrics (heap, threads, GC)

### Grafana Dashboards (Port 3000)

**Default Credentials**: admin/admin

**Pre-configured**:
- Prometheus datasource
- Microservices overview dashboard
- Service health monitoring
- Request rate & latency graphs

---

## Demo Credentials

### Employees (REST API)

| Name | Email | Password | Role |
|------|-------|----------|------|
| John Anderson | john.anderson@factory.com | Password123@ | employee |
| Sarah Williams | sarah.williams@factory.com | Password123@ | employee |
| Michael Chen | michael.chen@factory.com | Password123@ | employee |

### Customers (REST API)

| Name | Email | Password | Role |
|------|-------|----------|------|
| Alice Cooper | alice.cooper@customer.com | Password123@ | customer |
| Bob Smith | bob.smith@customer.com | Password123@ | customer |

### Telegram Bot Customers

**Auto-registered on `/start`**:
- Email: `customer_<telegram_id>@telegram.bot`
- Password: `telegram_<telegram_id>`
- Role: customer

---

## Troubleshooting

### Services Not Starting

```bash
# Check logs
docker-compose logs auth-service
docker-compose logs inventory-service
docker-compose logs order-service

# Restart specific service
docker-compose restart auth-service

# Rebuild and restart
docker-compose up --build -d auth-service
```

### Telegram Bot Not Responding

```bash
# Check bot logs
docker logs telegram_bot_service --tail 50

# Verify bot token
docker-compose exec telegram-bot-service env | grep TELEGRAM_BOT_TOKEN

# Restart bot
docker-compose restart telegram-bot-service
```

### Database Connection Issues

```bash
# Check database health
docker-compose exec auth-db pg_isready -U auth_user -d auth_db

# Connect to database
docker-compose exec auth-db psql -U auth_user -d auth_db

# Run migrations manually
docker-compose exec auth-service /app/migrate.sh
```

### JWT Token Issues

```bash
# Verify JWT secret is consistent
docker-compose exec auth-service env | grep JWT_SECRET
docker-compose exec inventory-service env | grep JWT_SECRET

# Test token generation
curl -X POST http://localhost:8081/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"john.anderson@factory.com","password":"Password123@"}'
```

---

## Project Structure

```
small-scale-factory-micro/
├── auth-service/
│   ├── src/main/kotlin/com/factory/auth/
│   │   ├── controllers/      # Route handlers
│   │   ├── services/         # Business logic
│   │   ├── repositories/     # Data access
│   │   ├── models/           # DTOs and entities
│   │   ├── plugins/          # Ktor plugins
│   │   └── Application.kt    # Entry point
│   ├── src/main/resources/
│   │   ├── db/migration/     # SQL migrations
│   │   └── db/seeder/        # Demo data
│   └── Dockerfile
│
├── inventory-service/
│   ├── src/main/kotlin/com/factory/inventory/
│   │   ├── controllers/
│   │   ├── services/
│   │   ├── repositories/
│   │   ├── models/
│   │   └── Application.kt
│   └── Dockerfile
│
├── order-service/
│   ├── src/main/kotlin/com/factory/order/
│   │   ├── controllers/
│   │   ├── services/
│   │   ├── repositories/
│   │   └── Application.kt
│   └── Dockerfile
│
├── telegram-bot-service/
│   ├── src/main/kotlin/com/factory/telegram/
│   │   ├── bot/              # Bot logic
│   │   ├── client/           # Service clients
│   │   └── Application.kt
│   └── Dockerfile
│
├── common-core/              # Shared code
│   └── src/main/kotlin/com/factory/common/
│       ├── config/
│       └── security/
│
├── docker-compose.yml
├── prometheus.yml
├── README.md
├── API_ENDPOINTS.md
└── PROJECT_REPORT.md
```

---

## Development Commands

```bash
# Build all services
./gradlew build

# Build specific service
./gradlew :auth-service:build
./gradlew :inventory-service:build

# Run tests
./gradlew test

# Build fat JARs
./gradlew :auth-service:fatJar

# Run locally (without Docker)
./gradlew :auth-service:run
```

---

## License

This project is for educational purposes as part of a university microservices architecture course.

---

## Contributors

- University Project Team
- DevOps Course 2025

---

## Additional Resources

- [Telegram Bot Setup Guide](docs/example/TELEGRAM_BOT_SETUP.md)
- [API Endpoints Documentation](./API_ENDPOINTS.md)
- [Project Report](./PROJECT_REPORT.md)
- [Ktor Documentation](https://ktor.io/)
- [Exposed ORM Guide](https://github.com/JetBrains/Exposed)
