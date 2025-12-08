# Step-by-Step Jenkins Setup Instructions

Follow these exact steps to connect your pipelines to Jenkins at localhost:8080

---

## ✅ Pre-Setup Verification

First, verify everything is ready:

```bash
cd /home/darksister/Documents/Project/devops/ktor

# 1. Check Jenkinsfiles exist
ls -l Jenkinsfile.auth Jenkinsfile.inventory

# 2. Check Jenkins is running
sudo systemctl status jenkins

# 3. Verify Jenkins can access Docker
sudo -u jenkins docker ps

# If Docker access fails, run:
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

---

## 📋 Step 1: Access Jenkins Web Interface

1. **Open your browser** and go to:
   ```
   http://localhost:8080
   ```

2. **Login** with your Jenkins credentials

3. You should see the Jenkins dashboard

---

## 📋 Step 2: Install Required Plugins (If Not Already Installed)

1. **Click**: `Manage Jenkins` (left sidebar)

2. **Click**: `Manage Plugins`

3. **Click**: `Available plugins` tab

4. **Search and install** these plugins (check the box, then click "Install"):
   - Git plugin
   - Docker plugin
   - Docker Pipeline plugin
   - Pipeline plugin
   - Multibranch Pipeline plugin
   - Blue Ocean (recommended for visual pipeline)
   - Gradle plugin
   - JUnit plugin

5. **Check**: "Restart Jenkins when installation is complete and no jobs are running"

6. **Wait** for Jenkins to restart (~1-2 minutes)

---

## 📋 Step 3: Configure Docker Hub Credentials

This is required for pushing Docker images.

### 3.1 Get Docker Hub Access Token (Recommended)

1. **Go to**: https://hub.docker.com/settings/security
2. **Click**: `New Access Token`
3. **Description**: "Jenkins CI/CD"
4. **Permissions**: "Read, Write, Delete"
5. **Click**: `Generate`
6. **Copy** the token (you won't see it again!)

### 3.2 Add Credentials to Jenkins

1. **In Jenkins**: `Manage Jenkins` → `Manage Credentials`

2. **Click**: `(global)` under "Stores scoped to Jenkins"

3. **Click**: `Add Credentials` (left sidebar)

4. **Fill in the form**:
   - **Kind**: `Username with password`
   - **Scope**: `Global`
   - **Username**: `your-dockerhub-username`
   - **Password**: `your-access-token-or-password`
   - **ID**: `docker-hub-credentials` ⚠️ **MUST BE EXACTLY THIS**
   - **Description**: `Docker Hub Credentials`

5. **Click**: `Create`

---

## 📋 Step 4: Create Jenkins Pipeline for Auth Service

### 4.1 Create New Job

1. **Go to Jenkins dashboard**: http://localhost:8080

2. **Click**: `New Item` (top left)

3. **Enter name**: `ktor-auth-service`

4. **Select**: `Multibranch Pipeline`

5. **Click**: `OK`

### 4.2 Configure Branch Sources

1. **Under "Branch Sources"**, click `Add source` → `Git`

2. **Project Repository**: Enter your Git repository URL
   ```bash
   # If local repo, use file path:
   file:///home/darksister/Documents/Project/devops/ktor

   # If GitHub/GitLab, use HTTPS URL:
   https://github.com/yourusername/your-repo.git
   ```

3. **Credentials**:
   - If **private repo**: Click `Add` → Add your Git credentials
   - If **public repo** or **local**: Leave as `- none -`

4. **Behaviors**: Keep defaults (or add "Discover branches" if not present)

### 4.3 Configure Build Configuration

1. **Scroll to "Build Configuration"**

2. **Mode**: `by Jenkinsfile` (should be default)

3. **Script Path**: `Jenkinsfile.auth` ⚠️ **EXACTLY THIS**

### 4.4 Configure Scan Triggers

1. **Scroll to "Scan Multibranch Pipeline Triggers"**

2. **Check**: ☑ `Periodically if not otherwise run`

3. **Interval**: Select `2 minutes` (for development)

### 4.5 Save

1. **Click**: `Save` (bottom of page)

2. Jenkins will automatically scan your repository and find branches

3. **Wait** for initial scan to complete (~30 seconds)

---

## 📋 Step 5: Create Jenkins Pipeline for Inventory Service

**Repeat Step 4**, but with these changes:

1. **Job name**: `ktor-inventory-service`
2. **Script Path**: `Jenkinsfile.inventory`
3. Everything else stays the same

---

## 📋 Step 6: Update Docker Image Names (Optional but Recommended)

Before running builds, update the Docker registry settings in your Jenkinsfiles:

### 6.1 Edit Jenkinsfile.auth

```bash
nano Jenkinsfile.auth
# or
code Jenkinsfile.auth
```

Find this section (around line 10-14):
```groovy
environment {
    // Docker Configuration
    DOCKER_REGISTRY = 'docker.io'
    DOCKER_IMAGE_NAME = 'ktor-auth-service'
    DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
```

**Change to**:
```groovy
environment {
    // Docker Configuration
    DOCKER_REGISTRY = 'docker.io'
    DOCKER_IMAGE_NAME = 'YOUR-DOCKERHUB-USERNAME/ktor-auth-service'
    DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
```

**Replace** `YOUR-DOCKERHUB-USERNAME` with your actual Docker Hub username.

### 6.2 Edit Jenkinsfile.inventory

Do the same for `Jenkinsfile.inventory`, changing:
```groovy
DOCKER_IMAGE_NAME = 'YOUR-DOCKERHUB-USERNAME/ktor-inventory-service'
```

### 6.3 Commit Changes

```bash
git add Jenkinsfile.auth Jenkinsfile.inventory
git commit -m "Configure Docker Hub username in Jenkinsfiles"
git push origin main
```

---

## 📋 Step 7: Trigger Your First Build

### 7.1 Manual Trigger

1. **Go to Jenkins dashboard**: http://localhost:8080

2. **Click on**: `ktor-auth-service`

3. **Click**: `Scan Multibranch Pipeline Now` (left sidebar)

4. **Wait** for scan to complete

5. **Click on your branch** (e.g., `main` or `develop`)

6. **Watch the build** run!

7. **Repeat** for `ktor-inventory-service`

### 7.2 View Build Progress

**Option A: Classic UI**
1. Click on the running build number (e.g., `#1`)
2. Click `Console Output`
3. Watch the logs in real-time

**Option B: Blue Ocean (Better!)**
1. Click `Open Blue Ocean` (left sidebar)
2. Visual pipeline view
3. Click on running build
4. See each stage progress

---

## 📋 Step 8: Verify Everything Works

### 8.1 Check Build Status

In Jenkins:
- ✅ Build should show **green checkmark** (success)
- ❌ Red = failed (see troubleshooting below)
- ⚠️ Yellow = unstable

### 8.2 Check Docker Images

```bash
# List Docker images built by Jenkins
docker images | grep ktor

# You should see:
# ktor-auth-service       latest    ...
# ktor-inventory-service  latest    ...
```

### 8.3 Check Services (if deployed)

```bash
# If you're on develop branch, services should be deployed
docker-compose ps

# Test health endpoints
curl http://localhost:8081/health  # Auth service
curl http://localhost:8082/health  # Inventory service
```

---

## 📋 Step 9: Set Up Automatic Builds (Choose One)

### Option A: GitHub Webhooks (Best for GitHub repos)

1. **Go to your GitHub repo** → `Settings` → `Webhooks`

2. **Click**: `Add webhook`

3. **Payload URL**:
   ```
   http://YOUR_JENKINS_IP:8080/github-webhook/
   ```

   **Note**: If Jenkins is on localhost and GitHub can't reach it:
   - Use ngrok: `ngrok http 8080`
   - Or use polling (already configured)

4. **Content type**: `application/json`

5. **Which events**: `Just the push event`

6. **Active**: ☑ Checked

7. **Click**: `Add webhook`

8. **Test**: Push a commit and watch Jenkins build automatically!

### Option B: GitLab Webhooks (Best for GitLab repos)

1. **Go to your GitLab project** → `Settings` → `Webhooks`

2. **URL**:
   ```
   http://YOUR_JENKINS_IP:8080/project/ktor-auth-service
   ```

3. **Trigger**: ☑ `Push events`

4. **Click**: `Add webhook`

### Option C: SCM Polling (Already Configured!)

Your Jenkinsfiles already have this:
```groovy
triggers {
    pollSCM('H/2 * * * *')  // Check every 2 minutes
}
```

**No action needed!** Jenkins will check for changes every 2 minutes automatically.

---

## 📋 Step 10: Test Automatic Build

### 10.1 Make a Simple Change

```bash
cd /home/darksister/Documents/Project/devops/ktor

# Add a comment to trigger build
echo "// Test Jenkins build" >> auth-service/src/main/kotlin/Application.kt

# Commit and push
git add .
git commit -m "Test Jenkins automatic build"
git push origin main
```

### 10.2 Watch Jenkins

1. **Go to**: http://localhost:8080
2. **Wait**: ~2 minutes (if using polling)
3. **Watch**: Build should start automatically
4. **Check**: Both auth and inventory pipelines

---

## 🎉 Success Checklist

You're fully set up when you can check all these:

- ☐ Jenkins accessible at localhost:8080
- ☐ Docker Hub credentials configured in Jenkins
- ☐ `ktor-auth-service` pipeline created
- ☐ `ktor-inventory-service` pipeline created
- ☐ Both pipelines show your branches
- ☐ Manual build succeeds (green checkmark)
- ☐ Docker images built successfully
- ☐ Automatic builds trigger on push
- ☐ Blue Ocean installed (optional but nice)

---

## 🐛 Troubleshooting

### Issue: "Jenkinsfile not found"

**Solution**:
```bash
# Verify files exist
ls -l Jenkinsfile.auth Jenkinsfile.inventory

# Check Script Path in Jenkins job config
# Should be: Jenkinsfile.auth (not ./Jenkinsfile.auth)

# Rescan repository
Jenkins → ktor-auth-service → Scan Multibranch Pipeline Now
```

### Issue: "Permission denied connecting to Docker daemon"

**Solution**:
```bash
# Give Jenkins Docker access
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins

# Wait 30 seconds, then verify
sudo -u jenkins docker ps
```

### Issue: "Docker push unauthorized"

**Solution**:
1. Check credentials ID is exactly: `docker-hub-credentials`
2. Re-enter Docker Hub credentials in Jenkins
3. Test Docker login:
   ```bash
   sudo -u jenkins docker login
   ```

### Issue: Build fails at "Build Auth Service" stage

**Solution**:
```bash
# Test build locally
cd /home/darksister/Documents/Project/devops/ktor
./gradlew :auth-service:build --stacktrace

# Fix any errors shown
# Then push again
```

### Issue: Can't access Jenkins at localhost:8080

**Solution**:
```bash
# Check Jenkins is running
sudo systemctl status jenkins

# If not running
sudo systemctl start jenkins

# Check port
sudo netstat -tlnp | grep 8080

# View logs
sudo journalctl -u jenkins -f
```

### Issue: "No credentials" error during Docker push

**Solution**:
1. Go to: `Manage Jenkins` → `Manage Credentials`
2. Verify credential exists with ID: `docker-hub-credentials`
3. Click on it → Update → Re-enter password/token
4. Rebuild pipeline

---

## 📞 Quick Commands Reference

```bash
# Jenkins
sudo systemctl status jenkins     # Check status
sudo systemctl restart jenkins    # Restart
sudo journalctl -u jenkins -f     # View logs

# Docker
docker-compose ps                 # Check services
docker-compose logs -f auth-service    # View logs
docker images | grep ktor         # List built images

# Build locally (test before pushing)
./gradlew :auth-service:build
./gradlew :auth-service:test

# Health checks
curl http://localhost:8081/health
curl http://localhost:8082/health
```

---

## 🎯 What Happens on Each Push?

### When you push to `main` branch:
1. ✅ Checkout code
2. ✅ Build service with Gradle
3. ✅ Run all tests
4. ✅ Build Fat JAR
5. ✅ Build Docker image
6. ✅ Push image to Docker Hub
7. ❌ **NO automatic deployment** (manual only)

### When you push to `develop` branch:
1. ✅ All steps from main branch, PLUS:
2. ✅ **Automatic deployment** to development environment
3. ✅ Health checks
4. ✅ Smoke tests

### When you push to `feature/*` branches:
1. ✅ Checkout code
2. ✅ Build service
3. ✅ Run tests
4. ❌ No Docker push
5. ❌ No deployment

---

## 🚀 Next Steps After Setup

1. **Install Blue Ocean** for better UI:
   ```
   Manage Jenkins → Manage Plugins → Available
   Search "Blue Ocean" → Install
   ```

2. **Read the comprehensive guide**:
   ```bash
   cat JENKINS_GUIDE.md
   ```

3. **Keep the quick reference handy**:
   ```bash
   cat JENKINS_QUICK_REFERENCE.md
   ```

4. **Test different branches**:
   ```bash
   git checkout -b feature/test-jenkins
   # Make changes
   git push origin feature/test-jenkins
   # Watch it build (no deploy)
   ```

---

## 📊 Understanding Your Pipeline Visualization

In Blue Ocean, you'll see stages like this:

```
[Checkout] → [Environment Info] → [Build] → [Test] → [Fat JAR] → [Docker Build] → [Push] → [Deploy] → [Smoke Tests]
   ✅            ✅                 ✅         ✅         ✅            ✅            ✅        ✅         ✅
```

- **Green ✅** = Stage passed
- **Red ❌** = Stage failed (click to see logs)
- **Gray ⊗** = Stage skipped (due to `when` conditions)

---

## 🎓 You're All Set!

Your Jenkins is now connected and configured! Every time you push code:
- **Jenkins automatically detects** the change (via webhook or polling)
- **Builds only the changed service** (independent pipelines)
- **Runs tests** and reports results
- **Builds Docker images** with proper versioning
- **Deploys automatically** to development (if develop branch)

**Test it now**:
```bash
echo "// Jenkins test" >> auth-service/src/main/kotlin/Application.kt
git add . && git commit -m "Test Jenkins" && git push origin main
```

Then watch: http://localhost:8080

Good luck! 🚀
