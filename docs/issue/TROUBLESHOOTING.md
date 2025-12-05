# Troubleshooting Guide - NatJoub Auth Service

## Common Issues and Solutions

### 1. "Address already in use" Error

**Error Message:**
```
Exception in thread "main" java.net.BindException: Address already in use
```

**Cause:** Port 8080 (or your configured PORT) is already being used by another process.

#### Solution A: Find and Kill the Process Using the Port

**Step 1: Find what's using port 8080**
```bash
# Linux/Mac
lsof -i :8080

# Or use netstat
netstat -tlnp | grep 8080

# Or use ss
ss -tlnp | grep 8080
```

**Step 2: Kill the process**
```bash
# Find the PID from the output above (example: 86927)
kill -9 <PID>

# Example:
kill -9 86927
```

**Step 3: Verify the port is free**
```bash
lsof -i :8080
# Should return nothing
```

**Step 4: Run your application**
```bash
./run-with-env.sh
```

#### Solution B: Use a Different Port

**Option 1: Change in .env file**
```bash
# Edit .env
nano .env

# Change this line:
PORT=8081  # or any available port
```

**Option 2: Set port temporarily**
```bash
PORT=8081 ./gradlew run
```

**Option 3: Use the helper script with custom port**
```bash
export PORT=8081
./run-with-env.sh
```

#### Solution C: Stop Docker Compose Services

If you ran `docker-compose up` before, the app might be running in Docker:

```bash
# Check running containers
docker ps

# Stop all containers
docker-compose down

# Now run locally
./run-with-env.sh
```

---

### 2. Database Connection Failed

**Error Messages:**
```
Failed to connect to database
Connection refused
```

#### Solution: Ensure PostgreSQL is Running

**Check if PostgreSQL is running:**
```bash
# Check with pg_isready
pg_isready -h 127.0.0.1 -p 5432

# Check Docker containers
docker ps | grep postgres
```

**Start PostgreSQL (if not running):**

**Option A: Using Docker**
```bash
docker run -d \
  --name postgres \
  -e POSTGRES_DB=microservice_db \
  -e POSTGRES_USER=pichgres \
  -e POSTGRES_PASSWORD=narinpich \
  -p 5432:5432 \
  postgres:16-alpine
```

**Option B: Start existing Docker container**
```bash
# List all containers (including stopped)
docker ps -a | grep postgres

# Start the container (replace with your container name/ID)
docker start <container_name_or_id>
```

**Option C: Using Docker Compose**
```bash
# Start only PostgreSQL
docker-compose up -d postgres

# Verify it's running
docker-compose ps
```

**Verify Database Credentials:**

Check your `.env` file matches the database:
```bash
cat .env | grep DATABASE
```

Should match your PostgreSQL setup:
```
DATABASE_URL=jdbc:postgresql://127.0.0.1:5432/microservice_db
DATABASE_USER=pichgres
DATABASE_PASSWORD=narinpich
```

**Test Connection:**
```bash
# Using psql
psql -h 127.0.0.1 -p 5432 -U pichgres -d microservice_db

# Using Docker
docker exec -it <postgres_container> psql -U pichgres -d microservice_db
```

---

### 3. Gradle Daemon Issues

**Error Messages:**
```
Gradle Daemon could not be reused
Failed to start Gradle daemon
```

#### Solution: Stop and Restart Gradle Daemon

```bash
# Stop all Gradle daemons
./gradlew --stop

# Check daemon status
./gradlew --status

# Run application
./gradlew run
```

---

### 4. OOM (Out of Memory) Errors

**Error Messages:**
```
java.lang.OutOfMemoryError: Java heap space
```

#### Solution: Increase JVM Memory

**Option A: Set JAVA_OPTS**
```bash
export JAVA_OPTS="-Xmx2g -Xms512m"
./gradlew run
```

**Option B: Edit gradle.properties**
```bash
# Create/edit gradle.properties
echo "org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m" >> gradle.properties
```

---

### 5. Migration Errors

**Error Messages:**
```
Failed to apply migration
Migration checksum mismatch
```

#### Solution: Reset Database

**For Development (CAUTION: Deletes all data):**

```bash
# Option A: Drop and recreate database
docker exec -it <postgres_container> psql -U pichgres -c "DROP DATABASE microservice_db;"
docker exec -it <postgres_container> psql -U pichgres -c "CREATE DATABASE microservice_db;"

# Option B: Remove Docker volume
docker-compose down -v
docker-compose up -d

# Option C: Manual SQL
psql -h 127.0.0.1 -p 5432 -U pichgres -d postgres -c "DROP DATABASE microservice_db;"
psql -h 127.0.0.1 -p 5432 -U pichgres -d postgres -c "CREATE DATABASE microservice_db;"
```

---

### 6. JWT Token Errors

**Error Messages:**
```
Invalid token
Token has expired
```

#### Solution: Check JWT Configuration

**Verify JWT secret is set:**
```bash
cat .env | grep JWT_SECRET
```

**Generate new JWT secret if needed:**
```bash
# Generate a secure secret
openssl rand -base64 64

# Add to .env
echo "JWT_SECRET=<generated_secret>" >> .env
```

**Check token expiry times:**
- Access tokens expire in 15 minutes
- Refresh tokens expire in 7 days
- Use `/v1/auth/refresh-token` to get new access token

---

### 7. CORS Errors (in Browser)

**Error Messages:**
```
Access to fetch has been blocked by CORS policy
```

#### Solution: Update CORS Configuration

**For Development (allow all origins):**

Already configured in `CORSPlugin.kt`:
```kotlin
anyHost() // Allows all origins
```

**For Production (specific origins):**

Edit `src/main/kotlin/com/natjoub/auth/plugins/CORSPlugin.kt`:
```kotlin
// Replace anyHost() with:
allowHost("yourdomain.com", schemes = listOf("https"))
allowHost("localhost:3000", schemes = listOf("http"))
```

---

### 8. Rate Limiting Errors

**Error Messages:**
```
429 Too Many Requests
Rate limit exceeded
```

#### Solution: Wait or Adjust Rate Limits

**Current limits:**
- Auth endpoints: 5 requests per minute
- API endpoints: 100 requests per minute

**To adjust (for development):**

Edit `src/main/kotlin/com/natjoub/auth/plugins/RateLimitPlugin.kt`:
```kotlin
// Increase limits for development
register(RateLimitName("auth")) {
    rateLimiter(limit = 100, refillPeriod = 1.minutes)
    // ...
}
```

**Or wait 1 minute and try again**

---

## Quick Diagnostic Commands

### Check Everything is Working

```bash
# 1. Check if PostgreSQL is running
pg_isready -h 127.0.0.1 -p 5432 || echo "PostgreSQL is NOT running"

# 2. Check if port 8080 is free
lsof -i :8080 || echo "Port 8080 is FREE"

# 3. Check environment variables
cat .env

# 4. Check Docker containers
docker ps

# 5. Test database connection
psql -h 127.0.0.1 -p 5432 -U pichgres -d microservice_db -c "SELECT 1;"

# 6. Check Gradle daemon
./gradlew --status
```

### Clean Start (Nuclear Option)

If everything is broken, start fresh:

```bash
# 1. Stop all Gradle daemons
./gradlew --stop

# 2. Stop and remove all Docker containers
docker-compose down -v

# 3. Clean build
./gradlew clean

# 4. Kill any processes on port 8080
kill -9 $(lsof -t -i:8080)

# 5. Start PostgreSQL fresh
docker run -d \
  --name postgres-fresh \
  -e POSTGRES_DB=microservice_db \
  -e POSTGRES_USER=pichgres \
  -e POSTGRES_PASSWORD=narinpich \
  -p 5432:5432 \
  postgres:16-alpine

# 6. Wait 5 seconds for DB to start
sleep 5

# 7. Run application
./run-with-env.sh
```

---

## Getting Help

### View Detailed Logs

```bash
# Run with debug output
./gradlew run --debug

# View only Ktor logs
./gradlew run 2>&1 | grep "ktor.application"

# View database logs
docker logs <postgres_container> -f
```

### Check Application Logs

```bash
# If running in Docker
docker-compose logs -f app

# If running locally
tail -f logs/application.log
```

### Common Log Locations

- **Application logs**: `logs/application.log`
- **Gradle logs**: `~/.gradle/daemon/<version>/daemon-<pid>.out.log`
- **Docker logs**: `docker logs <container_name>`

---

## Environment Variables Reference

Required variables in `.env`:

```bash
# Server
PORT=8080

# Database (MUST match your PostgreSQL setup)
DATABASE_URL=jdbc:postgresql://127.0.0.1:5432/microservice_db
DATABASE_USER=pichgres
DATABASE_PASSWORD=narinpich
DATABASE_MAX_POOL_SIZE=20

# JWT (generate with: openssl rand -base64 64)
JWT_SECRET=<your-secret-here>
JWT_ISSUER=http://0.0.0.0:8080/
JWT_AUDIENCE=http://0.0.0.0:8080/api

# Application
APP_ENVIRONMENT=development
ENABLE_SWAGGER=true
```

---

## Still Having Issues?

1. Check if PostgreSQL is actually running: `docker ps | grep postgres`
2. Check if the port is actually free: `lsof -i :8080`
3. Check your `.env` file credentials match PostgreSQL
4. Try the "Clean Start" procedure above
5. Check the logs for specific error messages
6. Review `AUTH_SERVICE_README.md` for setup instructions

---

## Pro Tips

✅ **Always check logs first**: Most errors have clear messages in logs
✅ **Verify environment variables**: Use `echo $PORT` or `cat .env`
✅ **Test database separately**: Don't assume PostgreSQL is running
✅ **Use Docker Compose for easy setup**: Handles everything automatically
✅ **Kill old processes**: Check for zombie processes on port 8080
✅ **Keep Gradle clean**: Run `./gradlew clean` when in doubt

---

Last updated: 2025-12-05
