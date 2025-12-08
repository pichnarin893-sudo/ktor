# Jenkins Quick Reference

Quick commands and workflows for daily development with Jenkins.

---

## 🚀 Daily Workflow

### Making Changes and Triggering Builds

```bash
# 1. Make your changes to code
vim auth-service/src/main/kotlin/...

# 2. Test locally first
./gradlew :auth-service:test

# 3. Commit and push
git add .
git commit -m "Update auth service feature"
git push origin develop

# 4. Jenkins will automatically build (if webhooks configured)
# Or manually trigger: Go to Jenkins → Scan Multibranch Pipeline Now
```

---

## 📋 Common Jenkins Tasks

### Check Build Status
```bash
# Open Jenkins
http://localhost:8080

# Quick view: Dashboard shows all pipelines
# Detailed view: Click pipeline → branch → build number
```

### Manually Trigger Build
1. Go to pipeline (e.g., `ktor-auth-service`)
2. Click "Scan Multibranch Pipeline Now"
3. Or click on branch → "Build Now"

### View Build Logs
1. Click on build number (#42)
2. Click "Console Output"
3. Or use Blue Ocean for visual view

### Cancel Running Build
1. Click on running build
2. Click "Abort" in left sidebar

---

## 🐳 Docker Commands

### Check Running Containers
```bash
docker-compose ps
```

### View Service Logs
```bash
# Auth service
docker-compose logs -f auth-service

# Inventory service
docker-compose logs -f inventory-service

# All services
docker-compose logs -f
```

### Restart Services
```bash
# Restart specific service
docker-compose restart auth-service

# Rebuild and restart
docker-compose up -d --build auth-service

# Restart all
docker-compose restart
```

### Clean Up Docker
```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Remove old images (frees disk space)
docker image prune -f

# Remove all unused containers, networks, images
docker system prune -a
```

---

## 🔧 Gradle Commands

### Build Specific Service
```bash
# Auth service
./gradlew :auth-service:build

# Inventory service
./gradlew :inventory-service:build

# Skip tests
./gradlew :auth-service:build -x test
```

### Run Tests
```bash
# All tests
./gradlew test

# Specific service
./gradlew :auth-service:test

# Specific test
./gradlew :auth-service:test --tests UserServiceTest

# With coverage
./gradlew test jacocoTestReport
```

### Build Fat JAR
```bash
# Auth service
./gradlew :auth-service:buildFatJar

# Inventory service
./gradlew :inventory-service:buildFatJar

# Output: [service]/build/libs/[service]-all.jar
```

### Clean Build
```bash
# Clean all
./gradlew clean

# Clean specific service
./gradlew :auth-service:clean

# Clean and rebuild
./gradlew clean build
```

---

## 🏥 Health Checks

### Test Service Endpoints
```bash
# Auth service health
curl http://localhost:8081/health

# Inventory service health
curl http://localhost:8082/health

# With details
curl -v http://localhost:8081/health
```

### Test After Deployment
```bash
# Check if services are responding
curl -f http://localhost:8081/health && echo "Auth: OK"
curl -f http://localhost:8082/health && echo "Inventory: OK"
```

---

## 🐛 Troubleshooting

### Jenkins Not Building

**Check if webhook is working:**
```bash
# GitHub: Repository → Settings → Webhooks → Recent Deliveries
# Look for 200 OK responses
```

**Force manual scan:**
1. Go to pipeline in Jenkins
2. Click "Scan Multibranch Pipeline Now"

**Check Jenkins logs:**
```bash
sudo journalctl -u jenkins -f
```

### Build Failing

**View full error:**
1. Click on failed build
2. Console Output
3. Look for ERROR or FAILED

**Test locally first:**
```bash
# Build
./gradlew :auth-service:build --stacktrace

# Test
./gradlew :auth-service:test --stacktrace
```

### Docker Permission Issues

**Verify Jenkins can access Docker:**
```bash
sudo -u jenkins docker ps
```

**Fix if needed:**
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Wait 30 seconds for Jenkins to restart
```

### Port Already in Use

**Find what's using the port:**
```bash
sudo lsof -i :8081
sudo lsof -i :8082
```

**Stop services:**
```bash
docker-compose down
# Or kill specific process
sudo kill -9 <PID>
```

### Service Won't Start

**Check logs:**
```bash
docker-compose logs auth-service
```

**Common issues:**
- Database not ready → Wait or check postgres logs
- Port conflict → Use `docker-compose down` first
- Missing environment variables → Check .env file

**Restart everything:**
```bash
docker-compose down
docker-compose up -d postgres
sleep 10  # Wait for DB
docker-compose up -d auth-service
docker-compose up -d inventory-service
```

---

## 📊 Monitoring

### Jenkins System Info
```bash
# Jenkins status
sudo systemctl status jenkins

# Jenkins logs
sudo journalctl -u jenkins -f

# Jenkins workspace
ls -la /var/lib/jenkins/workspace/
```

### Docker Stats
```bash
# Real-time stats
docker stats

# Disk usage
docker system df

# Container resource usage
docker-compose stats
```

### Application Logs
```bash
# Live logs
docker-compose logs -f

# Last 100 lines
docker-compose logs --tail=100 auth-service

# Since timestamp
docker-compose logs --since 2024-01-01T10:00:00
```

---

## 🔐 Security

### Update Docker Hub Credentials
1. Manage Jenkins → Manage Credentials
2. Click on `docker-hub-credentials`
3. Update → Save

### Rotate Secrets
```bash
# Generate new JWT secret
openssl rand -base64 64

# Update in docker-compose.yml or .env
JWT_SECRET=new-secret-here

# Restart services
docker-compose restart
```

---

## 📦 Backup & Restore

### Backup Jenkins Configuration
```bash
# Backup Jenkins home
sudo tar -czf jenkins-backup.tar.gz /var/lib/jenkins/

# Backup specific jobs
sudo tar -czf jobs-backup.tar.gz /var/lib/jenkins/jobs/
```

### Export Docker Images
```bash
# Save image
docker save ktor-auth-service:latest > auth-service.tar

# Load image
docker load < auth-service.tar
```

---

## 🔄 Git Workflow

### Feature Development
```bash
# Create feature branch
git checkout -b feature/new-auth-endpoint

# Make changes
# ...

# Test locally
./gradlew :auth-service:test

# Commit and push
git add .
git commit -m "Add new auth endpoint"
git push origin feature/new-auth-endpoint

# Jenkins will build feature branch (no deploy)
```

### Merge to Develop
```bash
# Switch to develop
git checkout develop
git pull origin develop

# Merge feature
git merge feature/new-auth-endpoint

# Push
git push origin develop

# Jenkins will build and deploy to development
```

### Release to Main
```bash
# Switch to main
git checkout main
git pull origin main

# Merge develop
git merge develop

# Tag release
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push
git push origin main --tags

# Jenkins will build and push Docker images (no auto-deploy)
```

---

## 📈 Performance Tips

### Speed Up Builds

**Enable Gradle caching:**
```bash
# In gradle.properties
org.gradle.caching=true
org.gradle.parallel=true
```

**Use Gradle daemon locally (not in CI):**
```bash
./gradlew --daemon
```

**Clean workspace periodically:**
```bash
sudo rm -rf /var/lib/jenkins/workspace/*/
```

### Reduce Docker Build Time

**Use layer caching:**
```bash
docker build --cache-from ktor-auth-service:latest -t ktor-auth-service:new .
```

**Prune regularly:**
```bash
# Remove unused images
docker image prune -f

# Remove everything unused
docker system prune -a -f
```

---

## 🎯 Quick Fixes

### "Jenkins is not responding"
```bash
sudo systemctl restart jenkins
```

### "Out of disk space"
```bash
docker system prune -a -f
sudo apt clean
./gradlew clean
```

### "Tests are failing in Jenkins but pass locally"
```bash
# Often environment differences
# Check: Java version, environment variables, database state

# Run tests in same environment
docker-compose run auth-service ./gradlew :auth-service:test
```

### "Docker push failed"
```bash
# Re-login to Docker Hub
sudo -u jenkins docker login

# Or update credentials in Jenkins
```

---

## 📞 Quick Links

### URLs (if running locally)
- **Jenkins**: http://localhost:8080
- **Blue Ocean**: http://localhost:8080/blue
- **Auth Service**: http://localhost:8081
- **Inventory Service**: http://localhost:8082
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000

### File Locations
- **Jenkinsfiles**: `Jenkinsfile.auth`, `Jenkinsfile.inventory`
- **Docker Compose**: `docker-compose.yml`
- **Gradle Build**: `build.gradle.kts`
- **Jenkins Home**: `/var/lib/jenkins/`
- **Jenkins Logs**: `/var/log/jenkins/jenkins.log`

---

## 🎓 Remember

### Before Pushing Code
1. ✅ Test locally: `./gradlew test`
2. ✅ Build locally: `./gradlew build`
3. ✅ Check formatting
4. ✅ Write meaningful commit messages

### When Build Fails
1. 🔍 Read console output carefully
2. 🔍 Test locally with same commands
3. 🔍 Check recent commits for issues
4. 🔍 Ask for help if stuck

### Best Practices
- 🚀 Commit often, push when ready
- 🚀 Write tests for new features
- 🚀 Keep builds fast (< 10 minutes)
- 🚀 Fix broken builds immediately
- 🚀 Don't commit secrets

---

## 🆘 Emergency Commands

### Stop Everything
```bash
# Stop all containers
docker-compose down

# Stop Jenkins
sudo systemctl stop jenkins

# Kill gradle daemons
./gradlew --stop
```

### Clean Everything
```bash
# Clean build artifacts
./gradlew clean

# Remove all containers
docker-compose down -v

# Remove all Docker resources
docker system prune -a -f

# Clean Jenkins workspace
sudo rm -rf /var/lib/jenkins/workspace/*
```

### Start Fresh
```bash
# Start from clean state
docker-compose down -v
./gradlew clean
docker-compose up -d

# Wait for health checks
sleep 30
curl http://localhost:8081/health
curl http://localhost:8082/health
```

---

**For detailed information, see [JENKINS_GUIDE.md](JENKINS_GUIDE.md)**
