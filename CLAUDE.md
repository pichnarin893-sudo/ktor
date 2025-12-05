# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A production-ready Ktor microservice implementing clean architecture with comprehensive monitoring, database integration, and JWT authentication. Built with Kotlin, using Exposed ORM for PostgreSQL, Koin for dependency injection, and Prometheus for metrics.

## Build & Development Commands

### Basic Commands
```bash
# Build the project
./gradlew build

# Run application locally
./gradlew run

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests UserServiceTest

# Create fat JAR
./gradlew buildFatJar
```

### Docker Development
```bash
# Start all services (app, PostgreSQL, Prometheus, Grafana)
docker-compose up --build

# Run in background
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Reset database (removes volumes)
docker-compose down -v
```

### Database Access
```bash
# Connect to PostgreSQL (when using docker-compose)
docker-compose exec postgres psql -U postgres -d microservice_db

# Common psql commands:
# \dt              - List tables
# \d users         - Describe users table
# SELECT * FROM users;
# \q               - Quit
```

## Architecture Overview

This project follows clean architecture with three distinct layers:

### 1. API Layer (`api/`)
- HTTP routes and request/response handling
- Located in `src/main/kotlin/com/example/api/routes/`
- Example: `UserRoutes.kt` defines REST endpoints for user operations
- Handles input validation and serialization

### 2. Domain Layer (`service/`, `model/`, `repository/`)
- **Services**: Business logic (e.g., `UserService.kt`)
- **Models**: Domain entities (e.g., `User.kt`)
- **Repository Interfaces**: Data access contracts (e.g., `UserRepository.kt`)
- Framework-agnostic - no Ktor or database dependencies

### 3. Data Layer (`db/`)
- **Repository Implementations**: `db/repository/UserRepositoryImpl.kt`
- **Database Tables**: `db/table/Users.kt` (Exposed table definitions)
- Database connection managed by `config/DatabaseFactory.kt` with HikariCP pooling

### Dependency Flow
```
API → Services → Repository Interface ← Repository Implementation → Database
```

### Application Bootstrap (`Application.kt`)
The application initializes in this order:
1. Load configuration from `application.conf` + environment variables
2. Initialize database connection pool via `DatabaseFactory.init()`
3. Setup Koin dependency injection with `appModule`
4. Install Ktor plugins (monitoring, serialization, HTTP, security, validation, routing)
5. Configure health checks

### Plugin Architecture (`plugins/`)
Ktor features are modularized:
- `Monitoring.kt` - Prometheus metrics and request logging
- `Serialization.kt` - JSON content negotiation
- `HTTP.kt` - CORS configuration
- `Security.kt` - JWT authentication
- `StatusPages.kt` - Global error handling
- `Validation.kt` - Request validation
- `Routing.kt` - Route registration
- `HealthCheck.kt` - Health endpoints

### Dependency Injection (`di/AppModule.kt`)
Uses Koin for DI. The `appModule` registers:
- Repositories as singletons with interface bindings
- Services as singletons with constructor injection

**When adding new components:**
1. Create interface in domain layer (if applicable)
2. Create implementation
3. Register in `appModule` using `single<Interface> { Implementation(get()) }`

## Key Technical Details

### Database
- **ORM**: Exposed (DSL + DAO)
- **Connection Pool**: HikariCP (configured in `DatabaseFactory.kt`)
- **Migrations**: Flyway (auto-runs on startup from `resources/db/migration/`)
- **Default connection**: PostgreSQL on `localhost:5432/microservice_db`

### Configuration
- Primary config: `src/main/resources/application.conf` (HOCON format)
- Environment variable overrides supported
- Key variables: `PORT`, `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `JWT_SECRET`
- Configuration loaded via `config/AppConfig.kt`

### Testing
- Unit tests in `src/test/kotlin/domain/service/` (e.g., `UserServiceTest.kt`)
- Integration tests in `src/test/kotlin/api/routes/` (e.g., `UserRoutesTest.kt`)
- Uses MockK for mocking
- H2 database for test isolation

### API Structure
- Base path: `/api/v1`
- Health endpoints: `/health`, `/health/ready`, `/health/live`
- Metrics: `/metrics` (Prometheus format)
- All endpoints return JSON
- Pagination supported via `?limit=X&offset=Y` query params

### Error Handling
- Global error handler in `plugins/StatusPages.kt`
- Custom exceptions in `exception/Exception.kt`
- Structured error responses with status codes

## Common Development Tasks

### Adding a New API Endpoint
1. Define route function in `api/routes/[Resource]Routes.kt`
2. Implement business logic in `service/[Resource]Service.kt`
3. Create repository interface in `repository/[Resource]Repository.kt`
4. Implement repository in `db/repository/[Resource]RepositoryImpl.kt`
5. Define table schema in `db/table/[Resource]s.kt` (Exposed table)
6. Create migration file in `resources/db/migration/` (format: `V[number]__description.sql`)
7. Register dependencies in `di/AppModule.kt`
8. Register routes in `plugins/Routing.kt`

### Adding a Database Table
1. Create table object in `db/table/` extending Exposed's `Table` or use DSL
2. Create migration SQL in `resources/db/migration/V[X]__table_name.sql`
3. Restart app (Flyway auto-migrates)

### Adding a New Dependency
1. Add to `build.gradle.kts` dependencies block
2. If it's a service/repository, register in `di/AppModule.kt`

### Modifying Authentication
- JWT configuration in `config/AppConfig.kt` (secret, issuer, audience)
- JWT validation in `plugins/Security.kt`
- Protected routes use `authenticate { }` block in route definitions

## Testing Strategy

### Running Tests
```bash
# All tests
./gradlew test

# Specific test
./gradlew test --tests UserServiceTest

# With coverage
./gradlew test jacocoTestReport
```

### Test Structure
- Service tests mock repositories and verify business logic
- Route tests use Ktor's `testApplication` for integration testing
- Database tests use H2 in-memory database

## Monitoring & Observability

### Health Checks
- `/health` - Overall health with database connectivity check
- `/health/ready` - Kubernetes readiness probe
- `/health/live` - Kubernetes liveness probe

### Metrics
- Prometheus metrics at `/metrics`
- Includes JVM metrics, HTTP request duration, request counts
- Grafana dashboards available at `http://localhost:3000` when using docker-compose

### Logging
- Logback configuration in `resources/logback.xml`
- Structured JSON logging via Logstash encoder
- Logs written to console and `logs/application.log`

## Important Conventions

### Package Structure
```
com.example/
├── api/routes/          # HTTP endpoints
├── service/             # Business logic
├── model/               # Domain models
├── repository/          # Data access interfaces
├── db/
│   ├── repository/      # Repository implementations
│   └── table/           # Database table definitions
├── config/              # Configuration
├── di/                  # Dependency injection
├── plugins/             # Ktor plugins
└── exception/           # Custom exceptions
```

### Naming Conventions
- Tables: Plural (e.g., `Users`)
- Models: Singular (e.g., `User`)
- Services: `[Entity]Service` (e.g., `UserService`)
- Repositories: `[Entity]Repository` interface, `[Entity]RepositoryImpl` implementation
- Routes: `[entity]Routes` function (e.g., `userRoutes()`)

### Code Organization
- Keep domain layer framework-agnostic (no Ktor/Exposed imports in `service/`, `model/`, `repository/`)
- Repository interfaces define data contracts without implementation details
- Services contain business logic and orchestrate repository calls
- Routes handle HTTP concerns (parsing, validation, serialization)

## Environment Variables Reference

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

## Security Considerations

- JWT_SECRET must be changed in production (generate with `openssl rand -base64 32`)
- Password hashing implementation placeholder exists - requires BCrypt implementation
- CORS configured in `plugins/HTTP.kt` - adjust allowed origins for production
- SQL injection protected by Exposed ORM's parameterized queries
- Input validation configured in `plugins/Validation.kt`

## Docker & Deployment

### Multi-stage Build
The `Dockerfile` uses multi-stage build:
1. Build stage: Compiles with Gradle
2. Runtime stage: Runs with JRE (smaller image)

### Docker Compose Services
- `app` - Ktor application (port 8080)
- `postgres` - PostgreSQL database (port 5432)
- `prometheus` - Metrics collection (port 9090)
- `grafana` - Metrics visualization (port 3000)

### Production Deployment
- Kubernetes deployment example in README.md
- Health probes configured for liveness/readiness
- Metrics endpoint for Prometheus scraping
- Stateless design enables horizontal scaling
