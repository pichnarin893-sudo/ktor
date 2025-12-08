# Jenkins CI/CD Guide for Ktor Microservices

Complete guide for setting up and using Jenkins with your Ktor microservices application.

## 📋 Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Pipeline Setup](#pipeline-setup)
- [Webhook Configuration](#webhook-configuration)
- [Best Practices](#best-practices)
- [Monitoring & Troubleshooting](#monitoring--troubleshooting)
- [Advanced Topics](#advanced-topics)

---

## 🎯 Overview

Your project has **two independent CI/CD pipelines**:

### **Auth Service Pipeline** (`Jenkinsfile.auth`)
- **Port**: 8081
- **Docker Image**: `ktor-auth-service`
- **Stages**: Checkout → Build → Test → Fat JAR → Docker Build → Push → Deploy

### **Inventory Service Pipeline** (`Jenkinsfile.inventory`)
- **Port**: 8082
- **Docker Image**: `ktor-inventory-service`
- **Stages**: Checkout → Build → Test → Fat JAR → Docker Build → Push → Deploy

**Benefits of Multi-Pipeline Approach:**
✅ Independent deployment cycles
✅ Faster builds (only changed service rebuilds)
✅ True microservice autonomy
✅ Better scalability

---

## ✅ Prerequisites

### System Requirements
- ✓ Jenkins installed and running
- ✓ Java 17 installed
- ✓ Docker installed and running
- ✓ Git installed
- ✓ Gradle wrapper in your project

### Jenkins User Permissions
```bash
# Verify Jenkins can access Docker
sudo -u jenkins docker ps

# If it fails, grant Docker access
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### Required Jenkins Plugins

Go to **Manage Jenkins** → **Manage Plugins** → **Available**

**Essential Plugins:**
- ☑ Git plugin
- ☑ Docker plugin
- ☑ Docker Pipeline plugin
- ☑ Pipeline plugin
- ☑ Multibranch Pipeline plugin
- ☑ Gradle plugin
- ☑ JUnit plugin
- ☑ JaCoCo plugin (code coverage)

**Recommended Plugins:**
- ☑ Blue Ocean (modern UI)
- ☑ GitHub plugin (for webhooks)
- ☑ Slack Notification plugin
- ☑ Pipeline Stage View plugin
- ☑ Build Monitor plugin

---

## 🚀 Quick Start

### Step 1: Run the Setup Helper

```bash
cd /home/darksister/Documents/Project/devops/ktor
./jenkins-project-setup.sh
```

This script will provide step-by-step instructions and verify your setup.

### Step 2: Configure Docker Hub Credentials

1. **Go to**: Manage Jenkins → Manage Credentials
2. **Click**: (global) domain → Add Credentials
3. **Select**: Username with password
4. **Enter**:
   - **Username**: Your Docker Hub username
   - **Password**: Your Docker Hub password or [access token](https://hub.docker.com/settings/security)
   - **ID**: `docker-hub-credentials` (must match Jenkinsfile)
   - **Description**: Docker Hub Credentials
5. **Click**: Create

**🔒 Security Tip**: Use Docker Hub access tokens instead of your password.

### Step 3: Update Jenkinsfiles

Edit the Docker registry settings in both Jenkinsfiles:

```groovy
environment {
    DOCKER_REGISTRY = 'docker.io'  // or your registry URL
    // Update image names if needed
    DOCKER_IMAGE_NAME = 'your-dockerhub-username/ktor-auth-service'
}
```

---

## 📦 Pipeline Setup

### Create Auth Service Pipeline

1. **Open Jenkins**: http://localhost:8080
2. **Click**: New Item
3. **Enter name**: `ktor-auth-service`
4. **Select**: Multibranch Pipeline
5. **Click**: OK

**Configure Branch Sources:**
- **Click**: Add source → Git
- **Project Repository**: Your Git repository URL
  ```
  https://github.com/yourusername/ktor-microservices.git
  ```
- **Credentials**: Add if private repo
- **Behaviors**: You can add behaviors like "Discover branches" strategy

**Build Configuration:**
- **Mode**: by Jenkinsfile
- **Script Path**: `Jenkinsfile.auth`

**Scan Multibranch Pipeline Triggers:**
- ☑ Periodically if not otherwise run
- **Interval**: 2 minutes (for development)
- ⚠️ This will be replaced by webhooks in production

**Click**: Save

### Create Inventory Service Pipeline

Repeat the same steps as above, but use:
- **Name**: `ktor-inventory-service`
- **Script Path**: `Jenkinsfile.inventory`

---

## 🪝 Webhook Configuration

Webhooks allow Git to automatically trigger Jenkins builds on push events, eliminating the need for polling.

### Option 1: GitHub Webhooks (Recommended)

#### Step 1: Install GitHub Plugin
```
Manage Jenkins → Manage Plugins → Available → GitHub plugin
```

#### Step 2: Configure GitHub Webhook

1. **Go to your GitHub repository** → Settings → Webhooks
2. **Click**: Add webhook
3. **Configure**:
   - **Payload URL**: `http://YOUR_JENKINS_URL:8080/github-webhook/`
   - **Content type**: application/json
   - **Which events**: Just the push event
   - ☑ Active
4. **Click**: Add webhook

**Example for local development (requires ngrok or public IP):**
```bash
# If Jenkins is on localhost, use ngrok
ngrok http 8080

# Use the ngrok URL in webhook:
# https://abc123.ngrok.io/github-webhook/
```

#### Step 3: Configure Jenkins Job

In each pipeline configuration:
1. **Branch Sources** → **Git** → **Behaviors**
2. **Add**: "Discover branches"
3. **Scan Multibranch Pipeline Triggers**:
   - ☐ Periodically if not otherwise run (disable polling)
4. **Save**

#### Step 4: Test Webhook

```bash
# Make a change and push
git add .
git commit -m "Test webhook"
git push

# Check Jenkins - build should start automatically
```

**Verify webhook delivery**:
- GitHub → Repository → Settings → Webhooks → Your webhook
- Click on it to see "Recent Deliveries"
- Look for 200 OK responses

---

### Option 2: GitLab Webhooks

#### Step 1: Install GitLab Plugin
```
Manage Jenkins → Manage Plugins → Available → GitLab plugin
```

#### Step 2: Configure GitLab Webhook

1. **Go to your GitLab project** → Settings → Webhooks
2. **Configure**:
   - **URL**: `http://YOUR_JENKINS_URL:8080/project/ktor-auth-service`
   - **Trigger**: Push events
   - **SSL verification**: Enable if using HTTPS
3. **Click**: Add webhook
4. **Test**: Click "Test" → "Push events"

---

### Option 3: SCM Polling (Fallback)

If webhooks aren't available (firewall, localhost), use SCM polling:

**In Jenkinsfile**, the trigger is already configured:
```groovy
triggers {
    pollSCM('H/2 * * * *')  // Poll every 2 minutes
}
```

**Or configure in Jenkins UI:**
1. Open pipeline configuration
2. **Scan Multibranch Pipeline Triggers**
3. ☑ Periodically if not otherwise run
4. **Interval**: 2 minutes

**Cron syntax examples:**
- `H/5 * * * *` - Every 5 minutes
- `H/15 * * * *` - Every 15 minutes
- `H * * * *` - Every hour

⚠️ **Note**: Polling is less efficient than webhooks but works for development.

---

## 💡 Best Practices for Development/Learning

### 1. Branch Strategy

**For development, use three branches:**

```
main/master     - Production-ready code (auto-deploy disabled)
develop         - Integration branch (auto-deploy to dev)
feature/*       - Feature branches (build + test only)
```

**Pipeline behavior by branch:**
- **main**: Build, Test, Docker Push (no auto-deploy)
- **develop**: Build, Test, Docker Push, Deploy to Dev
- **feature/**: Build, Test only

This is configured in the Jenkinsfiles with `when` conditions:
```groovy
stage('Deploy to Development') {
    when {
        branch 'develop'
    }
    // ...
}
```

### 2. Docker Image Management

**Use semantic tagging:**
```groovy
IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
// Example: 42-a1b2c3d
```

**Benefits:**
- Easy rollback to previous builds
- Clear version history
- Git commit traceability

**Cleanup old images:**
```bash
# Runs automatically in pipeline post-actions
docker image prune -f --filter "until=48h"
```

### 3. Service Dependencies

**Inventory service depends on Auth service**. The deploy stage handles this:

```groovy
stage('Deploy to Development') {
    steps {
        sh '''
            # Ensure auth-service is running first
            if ! docker-compose ps | grep -q "auth-service.*Up"; then
                docker-compose up -d auth-service
                sleep 10
            fi

            # Then start inventory-service
            docker-compose up -d inventory-service
        '''
    }
}
```

### 4. Test-Driven Development

**Run tests locally before pushing:**
```bash
# Test auth service
./gradlew :auth-service:test

# Test inventory service
./gradlew :inventory-service:test

# Run all tests
./gradlew test
```

**View test reports after Jenkins builds:**
- Navigate to build → Test Results
- Or use Blue Ocean for visual test results

### 5. Parallel Pipeline Execution

Since services are independent, you can trigger both pipelines simultaneously:
1. Commit changes to both services
2. Push to repository
3. Both pipelines run in parallel
4. Faster overall deployment

### 6. Environment Variables

**Keep secrets in Jenkins credentials, not in Jenkinsfiles:**

```groovy
environment {
    // Public configuration - OK in Jenkinsfile
    SERVICE_PORT = '8081'

    // Sensitive data - use credentials
    DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
}

// In stages, use withCredentials
withCredentials([usernamePassword(
    credentialsId: 'docker-hub-credentials',
    usernameVariable: 'DOCKER_USER',
    passwordVariable: 'DOCKER_PASS'
)]) {
    // Use $DOCKER_USER and $DOCKER_PASS
}
```

---

## 📊 Monitoring & Troubleshooting

### View Build Progress

**Classic UI:**
1. Open Jenkins dashboard
2. Click on pipeline name
3. Click on branch
4. Click on build number
5. View Console Output

**Blue Ocean (Recommended):**
1. Click "Open Blue Ocean" in left sidebar
2. Visual pipeline stages
3. Real-time log streaming
4. Better visualization of parallel stages

### Common Issues

#### ❌ **Issue**: `permission denied while trying to connect to Docker daemon`

**Solution:**
```bash
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

#### ❌ **Issue**: `Jenkinsfile not found`

**Solution:**
- Verify `Jenkinsfile.auth` and `Jenkinsfile.inventory` are in repository root
- Check "Script Path" in pipeline configuration
- Click "Scan Multibranch Pipeline Now" to refresh

#### ❌ **Issue**: `Docker push unauthorized`

**Solution:**
1. Verify Docker Hub credentials in Jenkins
2. Credential ID must match: `docker-hub-credentials`
3. Test Docker login manually:
   ```bash
   sudo -u jenkins docker login
   ```

#### ❌ **Issue**: `Port already in use`

**Solution:**
```bash
# Stop existing containers
docker-compose down

# Or stop specific service
docker-compose stop auth-service
docker-compose rm -f auth-service
```

#### ❌ **Issue**: `Build stuck at "Waiting for health check"`

**Solution:**
1. Check service logs:
   ```bash
   docker-compose logs -f auth-service
   ```
2. Verify health endpoint:
   ```bash
   curl http://localhost:8081/health
   ```
3. Check database connectivity

### Useful Jenkins Commands

```bash
# View Jenkins logs
sudo journalctl -u jenkins -f

# Restart Jenkins
sudo systemctl restart jenkins

# Check Jenkins status
sudo systemctl status jenkins

# View workspace
ls -la /var/lib/jenkins/workspace/

# Clean workspace manually
sudo rm -rf /var/lib/jenkins/workspace/ktor-auth-service/*
```

---

## 🔥 Advanced Topics

### 1. Adding Code Quality Checks

Add to your Jenkinsfiles:

```groovy
stage('Code Quality') {
    steps {
        sh '''
            # Kotlin linting with detekt
            ./gradlew detekt

            # Or add SonarQube scanning
            ./gradlew sonarqube \
                -Dsonar.host.url=http://localhost:9000 \
                -Dsonar.login=$SONAR_TOKEN
        '''
    }
}
```

### 2. Notifications

**Add Slack notifications** to `post` section:

```groovy
post {
    success {
        slackSend(
            color: 'good',
            message: "✅ Auth Service Build #${BUILD_NUMBER} succeeded"
        )
    }
    failure {
        slackSend(
            color: 'danger',
            message: "❌ Auth Service Build #${BUILD_NUMBER} failed"
        )
    }
}
```

**Setup**:
1. Install Slack Notification plugin
2. Manage Jenkins → Configure System → Slack
3. Add workspace and credentials

### 3. Kubernetes Deployment

If you have Kubernetes cluster, the pipelines already include K8s deployment:

```groovy
stage('Deploy to Kubernetes (Dev)') {
    when {
        branch 'main'
        expression { fileExists('k8s/deployment.yaml') }
    }
    steps {
        sh '''
            kubectl apply -f k8s/ --namespace=dev
            kubectl rollout status deployment/${APP_NAME} --namespace=dev
        '''
    }
}
```

**Setup**:
1. Create `k8s/` directory in your project
2. Add Kubernetes manifests (deployment.yaml, service.yaml)
3. Configure kubectl access in Jenkins

### 4. Multi-Environment Deployment

Extend pipelines to support staging and production:

```groovy
stage('Deploy to Staging') {
    when { branch 'staging' }
    steps {
        sh 'docker-compose -f docker-compose.staging.yml up -d'
    }
}

stage('Deploy to Production') {
    when { branch 'main' }
    steps {
        // Require manual approval
        input message: 'Deploy to production?', ok: 'Deploy'
        sh 'docker-compose -f docker-compose.prod.yml up -d'
    }
}
```

### 5. Shared Pipeline Libraries

For common pipeline logic, create a shared library:

**File**: `vars/buildMicroservice.groovy`
```groovy
def call(String serviceName, String port) {
    pipeline {
        agent any
        stages {
            stage('Build') {
                steps {
                    sh "./gradlew :${serviceName}:build"
                }
            }
            // Common stages...
        }
    }
}
```

**Usage in Jenkinsfile**:
```groovy
@Library('shared-pipeline') _
buildMicroservice('auth-service', '8081')
```

---

## 📈 Performance Optimization

### 1. Gradle Build Cache

Enable Gradle build cache in `gradle.properties`:
```properties
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.daemon=false  # Disable in CI
```

### 2. Docker Layer Caching

Use BuildKit for faster Docker builds:
```bash
export DOCKER_BUILDKIT=1
docker build --cache-from ktor-auth-service:latest ...
```

### 3. Parallel Test Execution

```groovy
stage('Test') {
    parallel {
        stage('Unit Tests') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Integration Tests') {
            steps {
                sh './gradlew integrationTest'
            }
        }
    }
}
```

---

## 🎓 Learning Resources

### Jenkins Documentation
- [Jenkins Official Docs](https://www.jenkins.io/doc/)
- [Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Blue Ocean Docs](https://www.jenkins.io/doc/book/blueocean/)

### Tutorials
- [Jenkins for Beginners](https://www.jenkins.io/doc/tutorials/)
- [Docker with Jenkins](https://www.jenkins.io/doc/book/pipeline/docker/)

### Your Pipeline Files
- `Jenkinsfile.auth` - Auth service pipeline
- `Jenkinsfile.inventory` - Inventory service pipeline
- `jenkins-project-setup.sh` - Quick setup helper

---

## 🆘 Getting Help

**Check pipeline logs:**
```bash
# Jenkins logs
sudo journalctl -u jenkins -f

# Service logs
docker-compose logs -f auth-service
docker-compose logs -f inventory-service
```

**Common commands:**
```bash
# Test build locally (without Jenkins)
./gradlew :auth-service:build
./gradlew :inventory-service:build

# Test Docker build
docker build -f auth-service/Dockerfile -t test-auth .

# Run services locally
docker-compose up --build
```

---

## ✅ Final Checklist

Before going to production, verify:

- ☐ Jenkins has Docker access
- ☐ Docker Hub credentials configured
- ☐ Both pipelines created and tested
- ☐ Webhooks configured (or polling enabled)
- ☐ Blue Ocean installed for better visualization
- ☐ Health checks passing
- ☐ Tests passing
- ☐ Docker images pushed successfully
- ☐ Services deploy correctly
- ☐ Notifications configured (optional)

---

## 📝 Summary

You now have:
✅ Two independent CI/CD pipelines for microservices
✅ Automated building, testing, and deployment
✅ Docker image management
✅ Webhook integration for automatic builds
✅ Best practices for development environments

**Next Steps:**
1. Run `./jenkins-project-setup.sh` to verify setup
2. Create your first pipeline in Jenkins
3. Push code and watch it build automatically
4. Explore Blue Ocean for modern UI
5. Add notifications and monitoring as needed

**Happy Building! 🚀**
