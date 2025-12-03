# Ktor Microservice - Production Ready Architecture

A production-ready Ktor microservice with clean architecture, comprehensive monitoring, and best practices.

## 🏗️ Architecture

This microservice follows clean architecture principles with clear separation of concerns:

```
src/
├── main/
│   ├── kotlin/com/example/
│   │   ├── api/              # API layer (Controllers/Routes)
│   │   ├── domain/           # Business logic
│   │   │   ├── model/        # Domain models
│   │   │   ├── repository/   # Repository interfaces
│   │   │   ├── service/      # Business services
│   │   │   └── exception/    # Custom exceptions
│   │   ├── data/             # Data layer
│   │   │   ├── repository/   # Repository implementations
│   │   │   └── table/        # Database tables
│   │   ├── config/           # Configuration
│   │   ├── di/               # Dependency injection
│   │   └── plugins/          # Ktor plugins
│   └── resources/
│       ├── application.conf  # Configuration
│       ├── logback.xml      # Logging configuration
│       └── db/migration/    # Database migrations
└── test/                    # Tests
```

## ✨ Features

### Core Features
- ✅ Clean Architecture (API → Domain → Data layers)
- ✅ Dependency Injection with Koin
- ✅ Database with Exposed ORM and connection pooling (HikariCP)
- ✅ Database migrations with Flyway
- ✅ JWT Authentication
- ✅ Input validation
- ✅ Comprehensive error handling

### Monitoring & Observability
- ✅ Prometheus metrics at `/metrics`
- ✅ Health checks (`/health`, `/health/ready`, `/health/live`)
- ✅ Structured JSON logging with Logstash encoder
- ✅ Request/response logging
- ✅ Grafana dashboards (via Docker Compose)

### API Features
- ✅ RESTful API design
- ✅ Content negotiation (JSON)
- ✅ CORS support
- ✅ API versioning (`/api/v1`)

### Development
- ✅ Docker support with multi-stage builds
- ✅ Docker Compose for local development
- ✅ Unit and integration tests
- ✅ Configuration via environment variables

## 🚀 Quick Start

### Prerequisites
- JDK 17+
- Docker & Docker Compose (for containerized setup)
- PostgreSQL (if running locally without Docker)

### Running with Docker Compose (Recommended)

```bash
# Build and start all services
docker-compose up --build

# The application will be available at:
# - API: http://localhost:8080
# - Prometheus: http://localhost:9090
# - Grafana: http://localhost:3000 (admin/admin)
```

### Running Locally

1. **Setup Database**
```bash
# Start PostgreSQL with Docker
docker run -d \
  --name postgres \
  -e POSTGRES_DB=microservice_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

2. **Build and Run**
```bash
# Build
./gradlew build

# Run
./gradlew run

# Or run the jar directly
java -jar build/libs/ktor-microservice-1.0.0-all.jar
```

## 📝 API Endpoints

### Health & Monitoring
```
GET  /health         - Health check with database status
GET  /health/ready   - Readiness probe
GET  /health/live    - Liveness probe
GET  /metrics        - Prometheus metrics
```

### Users API (v1)
```
GET    /api/v1/users              - List users (supports ?limit=10&offset=0)
GET    /api/v1/users/{id}         - Get user by ID
POST   /api/v1/users              - Create user
PUT    /api/v1/users/{id}         - Update user
DELETE /api/v1/users/{id}         - Delete user
```

### Example Requests

**Create User**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "johndoe",
    "password": "securepassword123"
  }'
```

**Get User**
```bash
curl http://localhost:8080/api/v1/users/1
```

**List Users**
```bash
curl "http://localhost:8080/api/v1/users?limit=10&offset=0"
```

## ⚙️ Configuration

Configuration is managed through `application.conf` with environment variable overrides.

### Key Environment Variables

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
ENABLE_SWAGGER=true
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test
./gradlew test --tests UserServiceTest
```

## 📊 Monitoring

### Prometheus Metrics
Access metrics at `http://localhost:8080/metrics`

Available metrics include:
- HTTP request duration
- Request count by endpoint
- JVM metrics (memory, threads, GC)
- Database connection pool metrics

### Grafana Dashboards
1. Access Grafana at `http://localhost:3000`
2. Login with `admin/admin`
3. Add Prometheus data source: `http://prometheus:9090`
4. Import dashboards for Ktor/JVM monitoring

### Logs
Logs are written to:
- Console (JSON format for production)
- `logs/application.log` (rotated daily)

## 🔒 Security Best Practices

1. **JWT Authentication**: Implement for protected routes
2. **Password Hashing**: Replace placeholder with BCrypt
3. **SQL Injection**: Protected by Exposed ORM
4. **CORS**: Configure allowed origins for production
5. **Rate Limiting**: Add rate limiting middleware
6. **Input Validation**: Comprehensive validation on all inputs

## 🏭 Production Deployment

### Docker Build
```bash
docker build -t ktor-microservice:latest .
```

### Environment Setup
1. Set strong `JWT_SECRET`
2. Configure database with SSL
3. Set `APP_ENVIRONMENT=production`
4. Configure CORS for your domain
5. Setup SSL/TLS termination (reverse proxy)
6. Configure log aggregation

### Kubernetes Deployment
```yaml
# Example deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ktor-microservice
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ktor-microservice
  template:
    metadata:
      labels:
        app: ktor-microservice
    spec:
      containers:
      - name: ktor-microservice
        image: ktor-microservice:latest
        ports:
        - containerPort: 8080
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        livenessProbe:
          httpGet:
            path: /health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

## 📚 Project Structure Details

### Layers

**API Layer** (`api/`): HTTP routes and request/response handling
- Route definitions
- Request/response DTOs
- Input validation

**Domain Layer** (`domain/`): Business logic (framework-agnostic)
- Domain models
- Repository interfaces
- Business services
- Custom exceptions

**Data Layer** (`data/`): Data access implementations
- Repository implementations
- Database table definitions
- Data mapping

### Dependency Flow
```
API → Domain ← Data
    ↓
  Plugins
    ↓
  Config
```

## 🛠️ Development Tips

1. **Adding New Endpoints**: Create routes in `api/routes/`
2. **Adding Business Logic**: Implement in `domain/service/`
3. **Database Changes**: Add migration in `resources/db/migration/`
4. **New Dependencies**: Register in Koin module (`di/AppModule.kt`)

## 📦 Build Artifacts

```bash
# Create fat jar
./gradlew buildFatJar

# Output: build/libs/ktor-microservice-1.0.0-all.jar
```

## 🤝 Contributing

1. Follow clean architecture principles
2. Write tests for new features
3. Update documentation
4. Follow Kotlin coding conventions

## 📄 License

MIT License - See LICENSE file for details

## 🔗 Useful Links

- [Ktor Documentation](https://ktor.io/docs)
- [Exposed Documentation](https://github.com/JetBrains/Exposed)
- [Koin Documentation](https://insert-koin.io/)
- [Prometheus](https://prometheus.io/)

## 💡 Next Steps

1. Implement proper password hashing (BCrypt)
2. Add refresh token mechanism
3. Implement rate limiting
4. Add API documentation (OpenAPI/Swagger)
5. Setup CI/CD pipeline
6. Add more comprehensive tests
7. Implement caching layer (Redis)
8. Add async messaging (Kafka/RabbitMQ)
