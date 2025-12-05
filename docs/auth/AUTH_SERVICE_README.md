# NatJoub Authentication Microservice

A production-ready role-based authentication microservice built with Ktor (Kotlin) for the NatJoub booking application.

## Features

- **Role-Based Authentication**: Separate JWT authentication for Admin, Seller, and Customer roles
- **Secure Password Storage**: BCrypt password hashing with 12 rounds
- **OTP Verification**: 6-digit OTP with 10-minute expiry for account verification
- **Token Management**: JWT access tokens (15 min) and refresh tokens (7 days) with blacklisting
- **Comprehensive Security**: Rate limiting, CORS, input validation, and SQL injection protection
- **Clean Architecture**: Separated layers (Controllers, Services, Repositories)
- **Production Ready**: Error handling, logging, monitoring, and testing

## Technology Stack

- **Framework**: Ktor 2.3.7
- **Language**: Kotlin 1.9.22
- **Database**: PostgreSQL with Exposed ORM
- **Authentication**: JWT (ktor-auth-jwt)
- **Password Hashing**: BCrypt (12 rounds)
- **Serialization**: Kotlinx Serialization
- **Dependency Injection**: Koin
- **Connection Pooling**: HikariCP
- **Testing**: JUnit 5, MockK

## Database Schema

### Tables

1. **roles** - User roles (admin, seller, customer)
2. **users** - Core user information
3. **credentials** - Authentication credentials and verification data
4. **refresh_tokens** - Refresh token storage for rotation
5. **token_blacklist** - Invalidated access tokens

See `V2__create_auth_tables.sql` for complete schema.

## API Endpoints

### Public Routes (`/v1/auth`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register new user (seller/customer only) |
| POST | `/login` | Login with email/username/phone + password |
| POST | `/verify-otp` | Verify OTP for account verification |
| POST | `/resend-otp` | Resend OTP to user |
| POST | `/forgot-password` | Request password reset |
| POST | `/reset-password` | Reset password with token |
| POST | `/refresh-token` | Refresh access token |

### Admin Routes (`/v1/admin/auth`) - Requires `admin-jwt`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | Get admin profile |
| PUT | `/profile` | Update admin profile |
| GET | `/users` | List all users (with role filtering) |
| PUT | `/users/{id}/status` | Activate/deactivate user |
| DELETE | `/users/{id}` | Delete user |
| POST | `/logout` | Logout and invalidate tokens |

### Seller Routes (`/v1/seller/auth`) - Requires `seller-jwt`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | Get seller profile |
| PUT | `/profile` | Update seller profile |
| GET | `/dashboard` | Get seller dashboard stats |
| POST | `/logout` | Logout seller |

### Customer Routes (`/v1/customer/auth`) - Requires `customer-jwt`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | Get customer profile |
| PUT | `/profile` | Update customer profile |
| GET | `/bookings` | Get customer bookings |
| POST | `/logout` | Logout customer |

## Setup and Installation

### Prerequisites

- JDK 17 or higher
- PostgreSQL 12 or higher
- Docker (optional, for containerized deployment)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ktor-microservice
   ```

2. **Set up PostgreSQL database**
   ```bash
   # Using Docker
   docker run -d \
     --name natjoub-postgres \
     -e POSTGRES_DB=microservice_db \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:16-alpine
   ```

3. **Configure environment variables** (optional)
   ```bash
   export DATABASE_URL=jdbc:postgresql://localhost:5432/microservice_db
   export DATABASE_USER=postgres
   export DATABASE_PASSWORD=postgres
   export JWT_SECRET=$(openssl rand -base64 64)
   export PORT=8080
   ```

4. **Build and run**
   ```bash
   # Build the project
   ./gradlew build

   # Run the application
   ./gradlew run
   ```

5. **Verify the service is running**
   ```bash
   curl http://localhost:8080/health
   # Expected: OK
   ```

### Docker Deployment

1. **Build Docker image**
   ```bash
   docker build -t natjoub-auth:latest .
   ```

2. **Run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

## Configuration

Configuration is managed via `application.conf` with environment variable overrides.

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
```

## API Usage Examples

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/v1/auth/register \
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
      "isActive": true,
      "isVerified": false
    },
    "expiresIn": 900
  },
  "message": "User registered successfully. Please verify your account with the OTP sent to your email."
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "john@example.com",
    "password": "SecureP@ss123"
  }'
```

### 3. Verify OTP

```bash
curl -X POST http://localhost:8080/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "john@example.com",
    "otp": "123456"
  }'
```

### 4. Access Protected Route

```bash
curl -X GET http://localhost:8080/v1/seller/auth/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

### 5. Refresh Access Token

```bash
curl -X POST http://localhost:8080/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
  }'
```

### 6. Logout

```bash
curl -X POST http://localhost:8080/v1/seller/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

## Security Features

### Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character

### Rate Limiting
- Authentication endpoints: 5 requests per minute per IP
- General API endpoints: 100 requests per minute per IP

### Token Management
- Access tokens expire after 15 minutes
- Refresh tokens expire after 7 days
- Tokens can be revoked on logout
- Blacklisted tokens are checked on each request

### Input Validation
- Email format validation
- Phone number validation (E.164 format)
- Username format (3-20 alphanumeric + underscore)
- Name validation (letters, spaces, hyphens, apostrophes)

## Testing

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test
```bash
./gradlew test --tests AuthFlowTest
```

### Test Coverage
```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## Project Structure

```
src/main/kotlin/com/natjoub/auth/
├── Application.kt                # Main entry point
├── config/                       # Configuration classes
│   ├── AppConfig.kt
│   └── DatabaseFactory.kt
├── controllers/                  # Route handlers
│   ├── PublicAuthRoutes.kt
│   ├── AdminRoutes.kt
│   ├── SellerRoutes.kt
│   └── CustomerRoutes.kt
├── models/                       # Data models
│   ├── dto/                      # Data Transfer Objects
│   └── entity/                   # Database entities
├── services/                     # Business logic
│   ├── AuthService.kt
│   └── UserService.kt
├── repositories/                 # Database operations
│   ├── UserRepository.kt
│   ├── CredentialRepository.kt
│   ├── RoleRepository.kt
│   └── TokenRepository.kt
├── utils/                        # Utility functions
│   ├── PasswordUtils.kt
│   ├── OTPUtils.kt
│   ├── JWTUtils.kt
│   └── ValidationUtils.kt
├── exceptions/                   # Custom exceptions
│   └── AuthExceptions.kt
├── plugins/                      # Ktor plugin configurations
│   ├── AuthPlugin.kt
│   ├── ErrorHandlingPlugin.kt
│   ├── RoutingPlugin.kt
│   ├── SerializationPlugin.kt
│   ├── CORSPlugin.kt
│   ├── RateLimitPlugin.kt
│   └── CallLoggingPlugin.kt
└── di/                          # Dependency injection
    └── AppModule.kt
```

## Error Handling

All errors return a standardized JSON response:

```json
{
  "success": false,
  "data": null,
  "message": "Error message",
  "error": {
    "code": "ERROR_CODE",
    "message": "Detailed error message",
    "details": {
      "field": "error detail"
    }
  }
}
```

### Common Error Codes

- `INVALID_CREDENTIALS` (401) - Wrong email/password
- `USER_NOT_FOUND` (404) - User doesn't exist
- `USER_ALREADY_EXISTS` (409) - Duplicate email/username/phone
- `INVALID_TOKEN` (401) - Invalid JWT token
- `EXPIRED_TOKEN` (401) - Token has expired
- `INSUFFICIENT_PERMISSIONS` (403) - Insufficient role permissions
- `VALIDATION_ERROR` (400) - Input validation failed
- `RATE_LIMIT_EXCEEDED` (429) - Too many requests
- `ACCOUNT_NOT_VERIFIED` (403) - Account needs OTP verification
- `ACCOUNT_DEACTIVATED` (403) - Account has been deactivated

## Production Deployment Checklist

- [ ] Change `JWT_SECRET` to a strong random value
- [ ] Update CORS allowed origins in `CORSPlugin.kt`
- [ ] Configure proper SSL/TLS certificates
- [ ] Set up database with SSL connections
- [ ] Configure log aggregation (ELK, CloudWatch, etc.)
- [ ] Set up monitoring and alerts (Prometheus, Grafana)
- [ ] Configure email/SMS service for OTP delivery
- [ ] Set up database backups
- [ ] Configure rate limiting based on traffic
- [ ] Review and adjust token expiry times
- [ ] Enable database connection encryption
- [ ] Set up health check endpoints for load balancer
- [ ] Configure proper logging levels for production

## Monitoring

### Health Checks
- `/health` - Basic health check
- `/metrics` - Prometheus metrics (if enabled)

### Logging
All authentication events are logged with appropriate levels:
- User registration
- Login attempts (success/failure)
- OTP generation and verification
- Token refresh
- Logout events
- Admin actions

## License

[Specify your license here]

## Support

For issues, questions, or contributions, please contact [your-contact-info].
