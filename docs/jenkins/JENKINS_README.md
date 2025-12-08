# Jenkins CI/CD for Ktor Microservices

Complete Jenkins setup for your Ktor-based microservices application with independent pipelines for Auth and Inventory services.

---

## 📂 What's Included

### Pipeline Files
- **`Jenkinsfile.auth`** - CI/CD pipeline for Auth Service (Port 8081)
- **`Jenkinsfile.inventory`** - CI/CD pipeline for Inventory Service (Port 8082)

### Documentation
- **`JENKINS_GUIDE.md`** - Complete setup and usage guide (📖 **START HERE**)
- **`JENKINS_QUICK_REFERENCE.md`** - Quick command reference for daily use
- **`JENKINS_README.md`** - This file (overview)

### Scripts
- **`jenkins-project-setup.sh`** - Interactive setup helper script

---

## 🚀 Quick Start (5 Minutes)

### 1. Prerequisites Check

```bash
# Verify Jenkins is running
sudo systemctl status jenkins
# Should show: Active: active (running)

# Verify Docker access for Jenkins
sudo -u jenkins docker ps
# Should list Docker containers (or show empty list)

# If Docker access fails, run:
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### 2. Run Setup Helper

```bash
cd /home/darksister/Documents/Project/devops/ktor
./jenkins-project-setup.sh
```

This script will:
- ✅ Verify Jenkinsfiles exist
- ✅ Check Docker permissions
- ✅ Provide step-by-step setup instructions

### 3. Create Jenkins Jobs

**Option A: Manual (Recommended for Learning)**
1. Open Jenkins: http://localhost:8080
2. Follow instructions from `jenkins-project-setup.sh` output
3. Or follow **Pipeline Setup** in `JENKINS_GUIDE.md`

**Option B: Automated (Coming Soon)**
- Use Jenkins Job DSL or Jenkins Configuration as Code (JCasC)

### 4. Configure Docker Hub Credentials

```
Manage Jenkins → Manage Credentials → Add Credentials
- Type: Username with password
- Username: [Your Docker Hub username]
- Password: [Your Docker Hub token]
- ID: docker-hub-credentials
```

### 5. Test Your Setup

```bash
# Make a small change
echo "// Test" >> auth-service/src/main/kotlin/Application.kt

# Commit and push
git add .
git commit -m "Test Jenkins pipeline"
git push origin develop

# Watch build in Jenkins
# Go to: http://localhost:8080
```

---

## 🏗️ Architecture Overview

### Multi-Pipeline Microservices Setup

```
┌─────────────────────────────────────────────────────────────┐
│                         Jenkins                              │
│                                                              │
│  ┌─────────────────────┐      ┌─────────────────────┐      │
│  │   Auth Service      │      │  Inventory Service  │      │
│  │   Pipeline          │      │  Pipeline           │      │
│  │                     │      │                     │      │
│  │  Jenkinsfile.auth   │      │ Jenkinsfile.       │      │
│  │                     │      │  inventory          │      │
│  └──────────┬──────────┘      └──────────┬─────────┘      │
└─────────────┼─────────────────────────────┼────────────────┘
              │                             │
              ▼                             ▼
         ┌─────────┐                   ┌─────────┐
         │ Auth    │                   │Inventory│
         │ Service │◄──────────────────│ Service │
         │ :8081   │  Depends on Auth  │ :8082   │
         └─────────┘                   └─────────┘
```

### Pipeline Stages

Each service pipeline includes:

1. **🔍 Checkout** - Clone repository
2. **📊 Environment Info** - Display build environment
3. **🔨 Build** - Gradle build (service-specific)
4. **🧪 Test** - Run unit and integration tests
5. **📦 Build Fat JAR** - Create executable JAR
6. **🐳 Build Docker Image** - Build service container
7. **🔍 Security Scan** - Optional Trivy scan
8. **🚀 Push Docker Image** - Push to registry (main/develop only)
9. **🚀 Deploy** - Deploy to development (develop branch only)
10. **✅ Smoke Tests** - Health checks

---

## 📋 Pipeline Behavior by Branch

| Branch | Build | Test | Docker Push | Deploy |
|--------|-------|------|------------|---------|
| **main** | ✅ | ✅ | ✅ | ❌ (Manual) |
| **develop** | ✅ | ✅ | ✅ | ✅ Auto |
| **feature/** | ✅ | ✅ | ❌ | ❌ |

**Key Points:**
- **Feature branches**: Build and test only (no deployment)
- **Develop branch**: Auto-deploy to development environment
- **Main branch**: Build and push images (deploy manually)

---

## 🔄 Triggering Builds

### Method 1: GitHub Webhooks (Recommended)

**Setup once:**
```
GitHub Repo → Settings → Webhooks → Add webhook
- URL: http://YOUR_JENKINS_IP:8080/github-webhook/
- Content type: application/json
- Events: Push events
```

**Then:**
```bash
git push origin develop
# Build starts automatically in Jenkins ⚡
```

### Method 2: SCM Polling (Automatic)

Already configured in Jenkinsfiles:
```groovy
triggers {
    pollSCM('H/2 * * * *')  // Check every 2 minutes
}
```

**No action needed** - Jenkins checks for changes automatically.

### Method 3: Manual Trigger

1. Go to Jenkins dashboard
2. Click pipeline name
3. Click "Scan Multibranch Pipeline Now"

---

## 🎯 Common Workflows

### Developing a New Feature

```bash
# 1. Create feature branch
git checkout -b feature/user-profile

# 2. Make changes to auth-service
vim auth-service/src/main/kotlin/...

# 3. Test locally
./gradlew :auth-service:test

# 4. Push to trigger Jenkins
git add .
git commit -m "Add user profile endpoint"
git push origin feature/user-profile

# 5. Jenkins runs: Build → Test (no deploy)
# 6. Check build status in Jenkins
```

### Deploying to Development

```bash
# 1. Merge feature to develop
git checkout develop
git merge feature/user-profile
git push origin develop

# 2. Jenkins automatically:
#    - Builds auth-service
#    - Runs tests
#    - Builds Docker image
#    - Pushes to Docker Hub
#    - Deploys to development

# 3. Verify deployment
curl http://localhost:8081/health
```

### Releasing to Main

```bash
# 1. Merge develop to main
git checkout main
git merge develop
git push origin main

# 2. Jenkins:
#    - Builds both services
#    - Runs all tests
#    - Pushes Docker images
#    - Does NOT auto-deploy

# 3. Deploy manually when ready
docker-compose pull
docker-compose up -d
```

---

## 📊 Monitoring Your Builds

### Classic Jenkins UI

```
http://localhost:8080
→ Click pipeline (e.g., ktor-auth-service)
→ Click branch (e.g., develop)
→ Click build number (e.g., #42)
→ Console Output
```

### Blue Ocean (Modern UI)

```
http://localhost:8080/blue
→ Visual pipeline view
→ Real-time log streaming
→ Better stage visualization
```

**Install Blue Ocean:**
```
Manage Jenkins → Manage Plugins → Available
Search: "Blue Ocean" → Install
```

---

## 🐛 Troubleshooting

### Build Failing?

**Check console output:**
```
Jenkins → Pipeline → Branch → Build # → Console Output
```

**Test locally first:**
```bash
./gradlew :auth-service:build --stacktrace
./gradlew :auth-service:test
```

### Docker Permission Error?

```bash
# Give Jenkins Docker access
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Verify
sudo -u jenkins docker ps
```

### Services Not Starting?

```bash
# Check logs
docker-compose logs -f auth-service

# Restart services
docker-compose down
docker-compose up -d

# Check health
curl http://localhost:8081/health
curl http://localhost:8082/health
```

**More troubleshooting**: See `JENKINS_QUICK_REFERENCE.md`

---

## 📚 Documentation Index

### For Setup and Configuration
👉 **[JENKINS_GUIDE.md](JENKINS_GUIDE.md)** - Complete guide
- Prerequisites and installation verification
- Pipeline setup (step-by-step)
- Webhook configuration (GitHub/GitLab)
- Best practices for development
- Advanced topics (K8s, notifications)

### For Daily Development
👉 **[JENKINS_QUICK_REFERENCE.md](JENKINS_QUICK_REFERENCE.md)** - Command reference
- Daily workflow
- Common Jenkins tasks
- Docker commands
- Gradle commands
- Troubleshooting
- Emergency commands

### For Understanding Pipelines
👉 **Jenkinsfile.auth** - Auth service pipeline code
👉 **Jenkinsfile.inventory** - Inventory service pipeline code

---

## 🎓 Learning Path

### Day 1: Setup
1. ✅ Run `./jenkins-project-setup.sh`
2. ✅ Create two Jenkins pipelines
3. ✅ Configure Docker Hub credentials
4. ✅ Trigger first build manually

### Day 2: Automation
1. ✅ Set up GitHub webhooks
2. ✅ Make a code change and push
3. ✅ Watch automatic build
4. ✅ Install Blue Ocean

### Day 3: Deployment
1. ✅ Push to develop branch
2. ✅ Watch auto-deployment
3. ✅ Verify services are running
4. ✅ Check health endpoints

### Day 4: Optimization
1. ✅ Review build times
2. ✅ Add code quality checks
3. ✅ Configure notifications
4. ✅ Set up monitoring

---

## 🔧 Configuration Files

### Jenkins Pipelines
```
Jenkinsfile.auth         - Auth service pipeline
Jenkinsfile.inventory    - Inventory service pipeline
```

### Docker
```
auth-service/Dockerfile      - Auth service container
inventory-service/Dockerfile - Inventory service container
docker-compose.yml           - Multi-service orchestration
```

### Build
```
build.gradle.kts         - Gradle build configuration
settings.gradle.kts      - Multi-module setup
gradle.properties        - Gradle properties
```

---

## 📞 Support

### Quick Help
```bash
# Setup helper
./jenkins-project-setup.sh

# Jenkins status
sudo systemctl status jenkins

# Service logs
docker-compose logs -f

# Test health
curl http://localhost:8081/health
curl http://localhost:8082/health
```

### Documentation
- 📖 Full Guide: `JENKINS_GUIDE.md`
- ⚡ Quick Ref: `JENKINS_QUICK_REFERENCE.md`
- 🏠 Jenkins Docs: https://www.jenkins.io/doc/

---

## ✅ Checklist Before Starting

### System Setup
- ☐ Jenkins installed and running
- ☐ Java 17 installed
- ☐ Docker installed and running
- ☐ Jenkins has Docker access
- ☐ Git repository accessible

### Jenkins Configuration
- ☐ Required plugins installed
- ☐ Docker Hub credentials configured
- ☐ Two pipelines created (auth, inventory)
- ☐ Webhooks or polling configured

### Repository
- ☐ Jenkinsfile.auth in repo root
- ☐ Jenkinsfile.inventory in repo root
- ☐ Both Dockerfiles exist
- ☐ docker-compose.yml configured

---

## 🚀 You're Ready!

Everything is set up for you to:
- ✅ Automatically build on code changes
- ✅ Run tests for each service independently
- ✅ Build and push Docker images
- ✅ Deploy to development automatically
- ✅ Monitor builds visually
- ✅ Scale to more microservices easily

**Next Step**: Run `./jenkins-project-setup.sh` and follow the instructions!

---

## 📈 Future Enhancements

As you grow more comfortable with Jenkins, consider:
- 🎯 Add staging environment
- 🎯 Implement blue-green deployments
- 🎯 Add performance testing stage
- 🎯 Integrate with Kubernetes
- 🎯 Add Slack/Discord notifications
- 🎯 Implement manual approval for production
- 🎯 Add security scanning (Trivy, SonarQube)
- 🎯 Set up multi-environment configs

All of these are covered in the Advanced Topics section of `JENKINS_GUIDE.md`.

---

**Happy Building! 🎉**

For questions or issues, refer to the comprehensive guide: **[JENKINS_GUIDE.md](JENKINS_GUIDE.md)**
