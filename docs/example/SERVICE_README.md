# NatJoub Microservices Platform

A production-ready modular monolith architecture built with Ktor (Kotlin) featuring multiple business services with schema-level isolation.

## 🏗️ Architecture Overview

This project follows a **modular monolith** pattern - a single application with multiple services sharing infrastructure but maintaining clear boundaries through PostgreSQL schemas.

### Service Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Ktor Application (Port 8080)              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              CORE INFRASTRUCTURE                       │  │
│  │  • Application Bootstrap                               │  │
│  │  • Shared Database Pool (HikariCP)                     │  │
│  │  • Configuration Management                            │  │
│  │  • Shared Plugins (CORS, Logging, Serialization)      │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Auth Service │  │   Inventory  │  │   Future     │      │
│  │              │  │   Service    │  │   Services   │      │
│  │ auth_schema  │  │inventory_sch│  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│           PostgreSQL Database: microservice_db               │
│  ┌──────────────┐  ┌──────────────┐                         │
│  │ auth_schema  │  │inventory_sch │                         │
│  │  • roles     │  │  • branches  │                         │
│  │  • users     │  │  • categories│                         │
│  │  • credentials│ │  • items     │                         │
│  │  • tokens    │  │  • stock     │                         │
│  └──────────────┘  └──────────────┘                         │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

- **Framework**: Ktor 2.3.7
- **Language**: Kotlin 1.9.22
- **Database**: PostgreSQL 16 with Exposed ORM
- **Authentication**: JWT (ktor-auth-jwt)
- **Password Hashing**: BCrypt (12 rounds)
- **Serialization**: Kotlinx Serialization
- **Dependency Injection**: Koin
- **Connection Pooling**: HikariCP
- **Testing**: JUnit 5, MockK
- **Monitoring**: Prometheus, Grafana

---

## 📦 Project Structure

```
src/main/kotlin/com/natjoub/
├── core/                           # SHARED INFRASTRUCTURE
│   ├── Application.kt              # Main entry point
│   ├── config/                     # Shared configuration
│   │   ├── AppConfig.kt            # Config loader
│   │   └── DatabaseFactory.kt     # Database pool manager
│   └── plugins/                    # Shared Ktor plugins
│       ├── SerializationPlugin.kt
│       ├── CORSPlugin.kt
│       ├── ErrorHandlingPlugin.kt
│       ├── CallLoggingPlugin.kt
│       ├── RateLimitPlugin.kt
│       └── RoutingPlugin.kt
│
├── auth/                           # AUTH SERVICE
│   ├── controllers/                # API route handlers
│   │   ├── PublicAuthRoutes.kt
│   │   ├── AdminRoutes.kt
│   │   ├── SellerRoutes.kt
│   │   └── CustomerRoutes.kt
│   ├── models/
│   │   ├── dto/                    # Request/Response DTOs
│   │   └── entity/                 # Database entities (auth_schema)
│   ├── services/                   # Business logic
│   │   ├── AuthService.kt
│   │   └── UserService.kt
│   ├── repositories/               # Database operations
│   │   ├── UserRepository.kt
│   │   ├── CredentialRepository.kt
│   │   ├── RoleRepository.kt
│   │   └── TokenRepository.kt
│   ├── utils/                      # Auth utilities
│   │   ├── PasswordUtils.kt
│   │   ├── OTPUtils.kt
│   │   ├── JWTUtils.kt
│   │   └── ValidationUtils.kt
│   ├── exceptions/                 # Custom exceptions
│   ├── plugins/                    # Auth-specific plugins
│   │   └── AuthPlugin.kt           # JWT authentication
│   └── di/
│       └── AppModule.kt            # Auth DI module
│
└── inventory/                      # INVENTORY SERVICE
    ├── controllers/
    │   └── InventoryRoutes.kt      # Inventory API routes
    ├── models/
    │   ├── dto/                    # Inventory DTOs
    │   └── entity/                 # Database entities (inventory_schema)
    ├── services/
    │   └── InventoryService.kt     # Business logic
    ├── repositories/               # Database operations
    │   ├── BranchRepository.kt
    │   ├── CategoryRepository.kt
    │   ├── InventoryItemRepository.kt
    │   ├── StockLevelRepository.kt
    │   └── StockMovementRepository.kt
    ├── exceptions/
    │   └── InventoryExceptions.kt
    └── di/
        └── InventoryModule.kt      # Inventory DI module
```

---

## 🗄️ Database Schema Architecture

### Single Database, Multiple Schemas

**Database**: `microservice_db`
**Connection**: Shared HikariCP pool (max 20 connections)

#### Auth Schema (`auth_schema`)

| Table | Description |
|-------|-------------|
| `roles` | User roles (admin, seller, customer) |
| `users` | Core user information |
| `credentials` | Authentication data (email, password, OTP) |
| `refresh_tokens` | JWT refresh token storage |
| `token_blacklist` | Invalidated access tokens |

#### Inventory Schema (`inventory_schema`)

| Table | Description |
|-------|-------------|
| `branches` | Physical store locations |
| `categories` | Product categories (hierarchical) |
| `inventory_items` | Products with SKU, pricing |
| `stock_levels` | Current stock per branch |
| `stock_movements` | Audit trail of stock transactions |

---

## 🚀 Services Documentation

### 1. Authentication Service (`auth`)

**Base Path**: `/api/v1/auth`
**Schema**: `auth_schema`
**Purpose**: User authentication, authorization, and account management

#### Features

- ✅ Role-based JWT authentication (Admin, Seller, Customer)
- ✅ Secure BCrypt password hashing
- ✅ OTP verification (6-digit, 10-min expiry)
- ✅ Token refresh and blacklisting
- ✅ Password reset workflow
- ✅ Rate limiting and input validation

#### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register new user |
| POST | `/login` | Login with credentials |
| POST | `/verify-otp` | Verify OTP |
| POST | `/resend-otp` | Resend OTP |
| POST | `/forgot-password` | Request password reset |
| POST | `/reset-password` | Reset password |
| POST | `/refresh-token` | Refresh access token |

#### Protected Endpoints (JWT Required)

**Admin Routes** (`/api/v1/admin/auth` - requires `admin-jwt`)
- GET `/profile` - Get admin profile
- PUT `/profile` - Update profile
- GET `/users` - List all users
- PUT `/users/{id}/status` - Activate/deactivate user
- DELETE `/users/{id}` - Delete user
- POST `/logout` - Logout

**Seller Routes** (`/api/v1/seller/auth` - requires `seller-jwt`)
- GET `/profile`, PUT `/profile`, GET `/dashboard`, POST `/logout`

**Customer Routes** (`/api/v1/customer/auth` - requires `customer-jwt`)
- GET `/profile`, PUT `/profile`, GET `/bookings`, POST `/logout`

#### Token Configuration

- **Access Token**: 15 minutes expiry
- **Refresh Token**: 7 days expiry
- **Algorithm**: HMAC256

---

### 2. Inventory Service (`inventory`)

**Base Path**: `/api/v1/inventory`
**Schema**: `inventory_schema`
**Purpose**: Multi-branch inventory and stock management

#### Features

- ✅ Multi-branch inventory tracking
- ✅ Hierarchical product categories
- ✅ SKU-based item management
- ✅ Stock level tracking with reservations
- ✅ Complete movement audit trail
- ✅ Low stock alerts
- ✅ Transfer between branches

#### Endpoints (All require JWT authentication)

**Branches** (`/branches`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create branch |
| GET | `/` | List all branches (paginated) |
| GET | `/{id}` | Get branch details |
| PUT | `/{id}` | Update branch |
| DELETE | `/{id}` | Delete branch |

**Categories** (`/categories`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create category |
| GET | `/` | List categories (paginated) |
| GET | `/{id}` | Get category details |
| PUT | `/{id}` | Update category |
| DELETE | `/{id}` | Delete category |

**Inventory Items** (`/items`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create inventory item |
| GET | `/` | List items (paginated) |
| GET | `/{id}` | Get item details |
| PUT | `/{id}` | Update item |
| DELETE | `/{id}` | Delete item |

**Stock Movements** (`/movements`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Record stock movement |
| GET | `/` | List movements (paginated) |
| GET | `/{id}` | Get movement details |

**Stock Levels** (`/stock-levels`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/branch/{branchId}` | Get stock by branch |
| GET | `/low-stock` | Get items below reorder level |

#### Movement Types

- **IN**: Stock received into branch
- **OUT**: Stock sold or used
- **TRANSFER**: Stock moved between branches
- **ADJUSTMENT**: Inventory count correction
- **RETURN**: Stock returned

---

## 🔧 Setup and Installation

### Prerequisites

- JDK 17 or higher
- PostgreSQL 12+ (or use Docker)
- Gradle 8.5+ (included via wrapper)
- Docker & Docker Compose (optional)

### Option 1: Local Development Setup

#### 1. Clone and Setup Database

```bash
# Clone repository
git clone <repository-url>
cd ktor-microservice

# Start PostgreSQL with Docker
docker run -d \
  --name natjoub-postgres \
  -e POSTGRES_DB=microservice_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

#### 2. Configure Environment Variables

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/microservice_db
export DATABASE_USER=postgres
export DATABASE_PASSWORD=postgres
export JWT_SECRET=$(openssl rand -base64 64)
export PORT=8080
```

#### 3. Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew run

# Or run fat JAR
./gradlew buildFatJar
java -jar build/libs/ktor-microservice-all.jar
```

#### 4. Run Database Migrations and Seeders

```bash
# Navigate to migration directory
cd src/main/resources/db

# Run migrations (creates schemas and tables)
./migrate.sh

# Run seeders (optional - for development data)
./seed.sh 001__mock_roles.sql
./seed.sh 002__mock_users.sql
./seed.sh 003__mock_credentials.sql
./seed.sh 004__mock_branches.sql
./seed.sh 005__mock_categories.sql
./seed.sh 006__mock_inventory_items.sql
./seed.sh 007__mock_stock_levels.sql
```

### Option 2: Docker Compose (Recommended)

```bash
# Start all services (app, PostgreSQL, Prometheus, Grafana)
docker-compose up --build

# Run in background
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down

# Reset database (removes volumes)
docker-compose down -v
```

**Services Available:**
- Application: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- PostgreSQL: localhost:5432

### 5. Verify Installation

```bash
# Health check
curl http://localhost:8080/health
# Expected: OK

# Check services info
curl http://localhost:8080/
# Expected: NatJoub Microservices is running
```

---

## 📝 Configuration

### Environment Variables

```bash
# Server
PORT=8080

# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/microservice_db
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
DATABASE_MAX_POOL_SIZE=20

# JWT
JWT_SECRET=your-secret-key-change-in-production
JWT_ISSUER=http://0.0.0.0:8080/
JWT_AUDIENCE=http://0.0.0.0:8080/api

# Application
APP_ENVIRONMENT=production
```

### Configuration Files

- `src/main/resources/application.conf` - Main HOCON config
- `src/main/resources/logback.xml` - Logging configuration
- `docker-compose.yml` - Docker services setup
- `prometheus.yml` - Prometheus scraping config

---

## 🧪 API Usage Examples

### Authentication Flow

#### 1. Register New User (Seller)

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "username": "johndoe",
    "password": "SecureP@ss123",
    "role": "seller",
    "phoneNumber": "+1234567890"
  }'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": "uuid",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "role": "seller",
      "isVerified": false
    },
    "expiresIn": 900
  }
}
```

#### 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "admin@example.com",
    "password": "admin123"
  }'
```

#### 3. Access Protected Route

```bash
# Save token from login response
TOKEN="eyJhbGciOiJIUzI1NiIs..."

# Access admin route
curl -X GET http://localhost:8080/api/v1/admin/auth/users \
  -H "Authorization: Bearer $TOKEN"
```

### Inventory Management Flow

#### 1. Create Branch

```bash
curl -X POST http://localhost:8080/api/v1/inventory/branches \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Downtown Store",
    "code": "DT-001",
    "address": "123 Main St",
    "city": "Phnom Penh",
    "country": "Cambodia",
    "phoneNumber": "+855123456789",
    "email": "downtown@example.com",
    "managerName": "Jane Smith"
  }'
```

#### 2. Create Inventory Item

```bash
curl -X POST http://localhost:8080/api/v1/inventory/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "LAPTOP-001",
    "name": "Dell Latitude 5420",
    "description": "14 inch laptop with Intel i5",
    "categoryId": "category-uuid",
    "unitOfMeasure": "piece",
    "unitPrice": "899.99",
    "reorderLevel": 5,
    "reorderQuantity": 10,
    "barcode": "123456789"
  }'
```

#### 3. Record Stock Movement (Stock IN)

```bash
curl -X POST http://localhost:8080/api/v1/inventory/movements \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inventoryItemId": "item-uuid",
    "toBranchId": "branch-uuid",
    "movementType": "IN",
    "quantity": 50,
    "unitPrice": "899.99",
    "referenceNumber": "PO-2024-001",
    "notes": "Initial stock"
  }'
```

#### 4. Check Stock Levels

```bash
curl -X GET "http://localhost:8080/api/v1/inventory/stock-levels/branch/{branchId}" \
  -H "Authorization: Bearer $TOKEN"
```

#### 5. Get Low Stock Items

```bash
curl -X GET http://localhost:8080/api/v1/inventory/stock-levels/low-stock \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔒 Security Features

### Authentication & Authorization

- JWT-based authentication with role separation
- BCrypt password hashing (12 rounds)
- Token blacklisting on logout
- Refresh token rotation

### Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character

### Rate Limiting

- Authentication endpoints: 5 requests/min per IP
- General API: 100 requests/min per IP

### Input Validation

- Email format validation
- Phone number validation (E.164)
- SQL injection protection (Exposed ORM)
- CORS configuration
- Request size limits

---

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Service Tests

```bash
# Auth service tests
./gradlew test --tests com.natjoub.auth.*

# Test coverage report
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## 📊 Monitoring & Observability

### Health Checks

- `/health` - Basic health check
- `/health/ready` - Readiness probe (Kubernetes)
- `/health/live` - Liveness probe (Kubernetes)

### Metrics (Prometheus)

Access metrics at http://localhost:9090

**Available Metrics:**
- HTTP request duration
- Request counts by endpoint
- JVM metrics (memory, GC, threads)
- Database connection pool stats

### Grafana Dashboards

Access dashboards at http://localhost:3000 (admin/admin)

### Logging

- Structured JSON logging (Logstash encoder)
- Logs location: `logs/application.log`
- Configurable via `logback.xml`

**Log Levels:**
- Production: INFO
- Development: DEBUG
- Sensitive operations: WARN/ERROR

---

## 🚢 Production Deployment

### Docker Build

```bash
# Build image
docker build -t natjoub-microservices:latest .

# Tag for registry
docker tag natjoub-microservices:latest registry.example.com/natjoub:1.0.0

# Push to registry
docker push registry.example.com/natjoub:1.0.0
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: natjoub-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: natjoub
  template:
    metadata:
      labels:
        app: natjoub
    spec:
      containers:
      - name: app
        image: registry.example.com/natjoub:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-secrets
              key: url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secrets
              key: secret
        livenessProbe:
          httpGet:
            path: /health/live
            port: 8080
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 8080
```

### Production Checklist

- [ ] Change `JWT_SECRET` to strong random value (`openssl rand -base64 64`)
- [ ] Update CORS allowed origins
- [ ] Configure SSL/TLS certificates
- [ ] Enable database SSL connections
- [ ] Set up log aggregation (ELK, CloudWatch)
- [ ] Configure Prometheus + Grafana monitoring
- [ ] Set up email/SMS for OTP delivery
- [ ] Configure automated database backups
- [ ] Review rate limiting based on traffic
- [ ] Set proper token expiry times
- [ ] Configure connection pooling for load
- [ ] Set up health check endpoints for load balancer
- [ ] Enable database connection encryption
- [ ] Review and adjust logging levels

---

## 🔄 Adding New Services

To add a new service (e.g., "Order Service"):

### 1. Create Package Structure

```bash
mkdir -p src/main/kotlin/com/natjoub/order/{models,repositories,services,controllers,di,exceptions}
```

### 2. Create Database Schema Migration

```sql
-- 003__create_order_schema.sql
CREATE SCHEMA IF NOT EXISTS order_schema;
-- Add tables...
```

### 3. Create Service Module

```kotlin
// order/di/OrderModule.kt
val orderModule = module {
    single { OrderRepository() }
    single { OrderService(get()) }
}
```

### 4. Register in Core Application

```kotlin
// core/Application.kt
install(Koin) {
    modules(
        appModule,
        inventoryModule,
        orderModule  // Add new module
    )
}
```

### 5. Add Routes

```kotlin
// core/plugins/RoutingPlugin.kt
fun Application.configureRouting() {
    routing {
        // ...
        orderRoutes()  // Add new routes
    }
}
```

---

## 📚 Additional Resources

- [Ktor Documentation](https://ktor.io/docs)
- [Exposed ORM](https://github.com/JetBrains/Exposed)
- [Koin DI](https://insert-koin.io/)
- [JWT.io](https://jwt.io/)

---

## 📄 License

[Specify your license here]

## 👥 Support

For issues, questions, or contributions:
- GitHub Issues: [repository-url]/issues
- Email: [your-contact-email]
- Documentation: [docs-url]

---

**Built with ❤️ using Kotlin & Ktor**
