# Complete Local Development Commands Guide

Here's a comprehensive list of commands to maintain and develop your Ktor microservice locally.

---

## 🚀 Starting & Stopping the Application

### With Docker Compose (Recommended)

```bash
# Start all services (app + database + monitoring)
docker-compose up

# Start in detached mode (background)
docker-compose up -d

# Start and rebuild containers
docker-compose up --build

# Stop all services
docker-compose down

# Stop and remove volumes (clears database)
docker-compose down -v

# View logs
docker-compose logs

# Follow logs in real-time
docker-compose logs -f

# View logs for specific service
docker-compose logs -f app
docker-compose logs -f postgres
```

### Without Docker (Local Development)

```bash
# Start PostgreSQL first (if not using Docker Compose)
docker run -d \
  --name postgres \
  -e POSTGRES_DB=microservice_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# Run the application
./gradlew run

# Run with auto-reload on code changes
./gradlew run --continuous

# Stop PostgreSQL
docker stop postgres
docker rm postgres
```

---

## 🔧 Building the Application

```bash
# Build the project
./gradlew build

# Build without running tests
./gradlew build -x test

# Clean build artifacts
./gradlew clean

# Clean and build
./gradlew clean build

# Create fat JAR (includes all dependencies)
./gradlew buildFatJar

# Build output location
ls build/libs/
```

---

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run specific test class
./gradlew test --tests UserServiceTest

# Run specific test method
./gradlew test --tests UserServiceTest."getUserById should return user when user exists"

# Run tests and generate coverage report
./gradlew test jacocoTestReport

# View test results
open build/reports/tests/test/index.html
```

---

## 🐳 Docker Commands

### Building Docker Image

```bash
# Build Docker image
docker build -t ktor-microservice:latest .

# Build with specific tag
docker build -t ktor-microservice:v1.0.0 .

# Build without cache
docker build --no-cache -t ktor-microservice:latest .
```

### Running Docker Container

```bash
# Run container
docker run -p 8080:8080 ktor-microservice:latest

# Run with environment variables
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/microservice_db \
  -e DATABASE_PASSWORD=postgres \
  ktor-microservice:latest

# Run in detached mode
docker run -d -p 8080:8080 --name ktor-app ktor-microservice:latest

# Stop container
docker stop ktor-app

# Remove container
docker rm ktor-app

# View container logs
docker logs ktor-app

# Follow container logs
docker logs -f ktor-app
```

### Managing Docker Resources

```bash
# List running containers
docker ps

# List all containers
docker ps -a

# List images
docker images

# Remove image
docker rmi ktor-microservice:latest

# Remove unused images
docker image prune

# Remove all stopped containers
docker container prune

# View resource usage
docker stats
```

---

## 💾 Database Management

### PostgreSQL Commands

```bash
# Connect to database (Docker Compose)
docker-compose exec postgres psql -U postgres -d microservice_db

# Connect to standalone PostgreSQL
docker exec -it postgres psql -U postgres -d microservice_db

# Common SQL commands inside psql:
\dt              # List tables
\d users         # Describe users table
SELECT * FROM users;
\q               # Quit

# Backup database
docker-compose exec postgres pg_dump -U postgres microservice_db > backup.sql

# Restore database
docker-compose exec -T postgres psql -U postgres microservice_db < backup.sql

# Drop and recreate database
docker-compose exec postgres psql -U postgres -c "DROP DATABASE microservice_db;"
docker-compose exec postgres psql -U postgres -c "CREATE DATABASE microservice_db;"
```

### Database Migrations

```bash
# Migrations run automatically on startup
# Located in: src/main/resources/db/migration/

# To manually run migrations (if needed)
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo

# Clean database (DANGER: deletes all data)
./gradlew flywayClean
```

---

## 🔍 Monitoring & Health Checks

```bash
# Check application health
curl http://localhost:8080/health

# Check readiness probe
curl http://localhost:8080/health/ready

# Check liveness probe
curl http://localhost:8080/health/live

# View Prometheus metrics
curl http://localhost:8080/metrics

# Pretty print health check
curl -s http://localhost:8080/health | jq

# Watch health status (updates every 2 seconds)
watch -n 2 'curl -s http://localhost:8080/health | jq'
```

---

## 🌐 API Testing Commands

### User CRUD Operations

```bash
# Get all users
curl http://localhost:8080/api/v1/users

# Get all users with pagination
curl "http://localhost:8080/api/v1/users?limit=10&offset=0"

# Get user by ID
curl http://localhost:8080/api/v1/users/1

# Create user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "username": "johndoe",
    "password": "password123"
  }'

# Create user with pretty output
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "username": "janedoe",
    "password": "password123"
  }' | jq

# Update user
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newemail@example.com",
    "username": "newusername"
  }'

# Delete user
curl -X DELETE http://localhost:8080/api/v1/users/1

# Check response status code
curl -I http://localhost:8080/api/v1/users
```

---

## 📝 Logging & Debugging

```bash
# View application logs (Docker Compose)
docker-compose logs app

# Follow logs in real-time
docker-compose logs -f app

# View last 100 lines
docker-compose logs --tail=100 app

# View logs with timestamps
docker-compose logs -t app

# View logs from specific time
docker-compose logs --since 2024-01-01T10:00:00 app

# View local log files (when running without Docker)
tail -f logs/application.log

# Watch log file with colors
tail -f logs/application.log | grep --color=auto "ERROR\|WARN\|INFO"

# Search logs for errors
docker-compose logs app | grep ERROR

# Count log levels
docker-compose logs app | grep -c ERROR
docker-compose logs app | grep -c WARN
docker-compose logs app | grep -c INFO
```

---

## 🔄 Development Workflow Commands

### Code Changes

```bash
# Auto-reload on code changes (Gradle)
./gradlew run --continuous

# Quick rebuild and restart (Docker Compose)
docker-compose restart app

# Full rebuild after code changes
docker-compose up --build

# Rebuild specific service
docker-compose up --build app
```

### Dependency Management

```bash
# Download dependencies
./gradlew dependencies

# Update dependencies
./gradlew dependencyUpdates

# Show dependency tree
./gradlew dependencies --configuration runtimeClasspath

# Refresh dependencies (force download)
./gradlew build --refresh-dependencies
```

### Code Quality

```bash
# Run Kotlin linter (if configured)
./gradlew ktlintCheck

# Format code (if configured)
./gradlew ktlintFormat

# Static code analysis (if configured)
./gradlew detekt
```

---

## 🧹 Cleanup Commands

```bash
# Remove build artifacts
./gradlew clean

# Stop and remove all containers
docker-compose down

# Remove containers and volumes (database data)
docker-compose down -v

# Remove all Docker resources for this project
docker-compose down -v --rmi all

# Clean Gradle cache
rm -rf ~/.gradle/caches/

# Remove log files
rm -rf logs/

# Full cleanup
./gradlew clean
docker-compose down -v
rm -rf logs/
```

---

## 🔐 Security & Configuration

### Environment Variables

```bash
# View current .env file
cat .env

# Edit .env
nano .env

# Generate secure JWT secret
openssl rand -base64 32

# Load .env and run
export $(cat .env | xargs) && ./gradlew run

# Run with specific environment
APP_ENVIRONMENT=production ./gradlew run
```

### Switching Environments

```bash
# Switch to development
cp .env.development .env
docker-compose restart

# Switch to production
cp .env.production .env
docker-compose restart

# Use specific env file
docker-compose --env-file .env.production up
```

---

## 📊 Performance & Monitoring

```bash
# Check memory usage
docker stats

# Check container resource usage
docker-compose top

# Monitor system resources
htop  # or top

# Load testing with Apache Bench
ab -n 1000 -c 10 http://localhost:8080/api/v1/users

# Load testing with curl (simple)
for i in {1..100}; do curl http://localhost:8080/health; done

# Continuous health monitoring
while true; do curl -s http://localhost:8080/health | jq .status; sleep 1; done
```

---

## 🐛 Troubleshooting Commands

```bash
# Check if port 8080 is in use
lsof -i :8080
# or
netstat -an | grep 8080

# Kill process on port 8080
kill -9 $(lsof -t -i:8080)

# Check Docker daemon status
docker info

# Check disk space
df -h

# Check Docker disk usage
docker system df

# Verify database connection
docker-compose exec postgres pg_isready -U postgres

# Test database connection from app
docker-compose exec app nc -zv postgres 5432

# Check network connectivity
docker-compose exec app ping postgres

# Inspect container
docker inspect ktor-app

# View container filesystem
docker-compose exec app ls -la

# Access container shell
docker-compose exec app /bin/sh

# View environment variables in container
docker-compose exec app env
```

---

## 📦 Gradle Wrapper Commands

```bash
# Check Gradle version
./gradlew --version

# List all tasks
./gradlew tasks

# List all tasks with details
./gradlew tasks --all

# Show project info
./gradlew projects

# Show dependency insight
./gradlew dependencyInsight --dependency <dependency-name>

# Gradle daemon management
./gradlew --stop        # Stop Gradle daemon
./gradlew --status      # Check daemon status
```

---

## 🚀 Quick Command Cheatsheet

```bash
# Start everything
docker-compose up -d

# View logs
docker-compose logs -f

# Restart after code changes
docker-compose restart app

# Run tests
./gradlew test

# Check health
curl http://localhost:8080/health

# Create test user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","username":"test","password":"password123"}'

# Stop everything
docker-compose down

# Full cleanup
docker-compose down -v && ./gradlew clean
```

---

## 📱 Useful Aliases (Add to ~/.bashrc or ~/.zshrc)

```bash
# Add these to your shell config for quick access
alias dc='docker-compose'
alias dcu='docker-compose up'
alias dcd='docker-compose down'
alias dcl='docker-compose logs -f'
alias dcr='docker-compose restart'
alias gw='./gradlew'
alias gwt='./gradlew test'
alias gwb='./gradlew build'
alias health='curl -s http://localhost:8080/health | jq'
```

---

## 🎯 Daily Development Workflow

```bash
# Morning: Start your day
docker-compose up -d
curl http://localhost:8080/health

# During development: Make changes and test
# (edit code)
docker-compose restart app
./gradlew test
curl http://localhost:8080/api/v1/users

# Before committing: Run tests and cleanup
./gradlew clean test
docker-compose logs app | grep ERROR

# End of day: Stop services
docker-compose down
```

---

## 📋 Common Scenarios

### Scenario 1: Fresh Start

```bash
# Complete fresh start
docker-compose down -v
./gradlew clean
docker-compose up --build
```

### Scenario 2: Code Changes

```bash
# Quick restart for code changes
docker-compose restart app

# Full rebuild if dependencies changed
docker-compose up --build app
```

### Scenario 3: Database Reset

```bash
# Reset database to clean state
docker-compose down -v
docker-compose up -d postgres
# Wait a few seconds
docker-compose up -d app
```

### Scenario 4: Debug Database Issues

```bash
# Check database is running
docker-compose ps postgres

# Connect to database
docker-compose exec postgres psql -U postgres -d microservice_db

# Check tables
\dt

# View users
SELECT * FROM users;
```

### Scenario 5: Performance Testing

```bash
# Start monitoring
docker stats

# In another terminal, run load test
for i in {1..1000}; do 
  curl -s http://localhost:8080/health > /dev/null
  echo "Request $i completed"
done

# Check logs for errors
docker-compose logs app | grep ERROR
```

### Scenario 6: Production Deployment Preparation

```bash
# Run all tests
./gradlew clean test

# Build production image
docker build -t ktor-microservice:v1.0.0 .

# Test production image locally
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/microservice_db \
  -e JWT_SECRET=$(openssl rand -base64 32) \
  -e APP_ENVIRONMENT=production \
  ktor-microservice:v1.0.0

# Run security scan (if configured)
# docker scan ktor-microservice:v1.0.0
```

---

## 🔍 Debugging Tips

### Enable Debug Logging

Edit `src/main/resources/logback.xml`:

```xml
<logger name="com.example" level="DEBUG"/>
```

Then restart:

```bash
docker-compose restart app
docker-compose logs -f app
```

### Debug Gradle Build

```bash
# Run with debug output
./gradlew build --debug

# Run with stack traces
./gradlew build --stacktrace
```

### Check Application Startup

```bash
# Watch startup logs
docker-compose up app | grep "Application started"

# Check if port is listening
lsof -i :8080
```

### Network Debugging

```bash
# Check container network
docker network ls
docker network inspect ktor-microservice_app-network

# Test connectivity between containers
docker-compose exec app ping postgres
docker-compose exec app nc -zv postgres 5432
```

---

## 📊 Monitoring Dashboard Access

```bash
# Application API
open http://localhost:8080

# Health Check
open http://localhost:8080/health

# Prometheus Metrics
open http://localhost:8080/metrics

# Prometheus UI
open http://localhost:9090

# Grafana Dashboard
open http://localhost:3000
# Login: admin / admin
```

---

## 🎓 Learning Resources Commands

```bash
# View all Gradle tasks
./gradlew tasks --all

# View project structure
tree -L 3 src/

# View dependencies
./gradlew dependencies --configuration runtimeClasspath

# Generate project report
./gradlew projectReport
```

---

## ⚡ Power User Commands

```bash
# One-liner to restart everything fresh
docker-compose down -v && docker-compose up --build -d && docker-compose logs -f app

# One-liner to test API after startup
docker-compose up -d && sleep 5 && curl http://localhost:8080/health | jq

# One-liner to run tests and show results
./gradlew clean test && open build/reports/tests/test/index.html

# One-liner to backup database
docker-compose exec postgres pg_dump -U postgres microservice_db | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz

# One-liner to check everything is healthy
curl -s http://localhost:8080/health | jq '.status' && docker-compose ps
```

---

## 📖 Command Categories Quick Index

- **Start/Stop**: `docker-compose up/down`
- **Build**: `./gradlew build`
- **Test**: `./gradlew test`
- **Database**: `docker-compose exec postgres psql`
- **Logs**: `docker-compose logs -f`
- **Health**: `curl http://localhost:8080/health`
- **API Test**: `curl http://localhost:8080/api/v1/users`
- **Cleanup**: `docker-compose down -v`

---

**Save this guide and refer to it anytime! 📚**

For more detailed information, see:
- [README.md](README.md) - Project overview
- [QUICKSTART.md](QUICKSTART.md) - Quick start guide
- [ENV_SETUP_GUIDE.md](ENV_SETUP_GUIDE.md) - Environment configuration
- [PRODUCTION_CHECKLIST.md](PRODUCTION_CHECKLIST.md) - Production deployment