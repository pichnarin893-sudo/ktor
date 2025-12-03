# GitHub Actions CI/CD Documentation

## 🚀 Workflows Overview

### 1. **CI/CD Pipeline** (`ci-cd.yml`)
Main continuous integration and deployment workflow.

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`

**Jobs:**
1. **test** - Runs tests with PostgreSQL database
   - Sets up PostgreSQL service
   - Creates database schema
   - Runs all tests
   - Uploads test results

2. **build** - Builds application JAR
   - Compiles code
   - Creates fat JAR
   - Uploads artifacts

3. **docker-build** - Builds and pushes Docker image
   - Only on push events
   - Pushes to GitHub Container Registry
   - Tags with branch name and commit SHA

### 2. **Tests** (`tests.yml`)
Separate test workflow for comprehensive testing.

**Triggers:**
- Push to `main`, `develop`, `feature/*` branches
- Pull requests

**Jobs:**
1. **unit-tests** - Runs unit tests only
2. **integration-tests** - Runs integration tests with database

### 3. **Code Quality** (`code-quality.yml`)
Security and quality checks.

**Triggers:**
- Push to `main` or `develop`
- Pull requests
- Weekly schedule (Sunday)

**Jobs:**
1. **security-scan** - Scans code for vulnerabilities
2. **docker-security** - Scans Docker image
3. **dependency-check** - Analyzes dependencies

### 4. **Release** (`release.yml`)
Creates releases and publishes artifacts.

**Triggers:**
- Tag push matching `v*.*.*` (e.g., v1.0.0)

**Jobs:**
1. **create-release** - Creates GitHub release
   - Builds JAR
   - Generates changelog
   - Creates release with artifacts
   - Builds and tags Docker image

---

## 🔧 Setup Instructions

### Prerequisites
1. GitHub repository
2. GitHub Container Registry enabled

### Required Secrets
No secrets required for basic setup! GitHub automatically provides:
- `GITHUB_TOKEN` - For pushing images and creating releases

### Optional Secrets (for deployment)
Add these in `Settings > Secrets and variables > Actions`:

- `SSH_PRIVATE_KEY` - SSH key for server deployment
- `SERVER_HOST` - Production server hostname
- `SERVER_USER` - SSH username
- `DEPLOY_KEY` - Deployment authentication key

---

## 📦 Docker Images

Images are published to GitHub Container Registry:
```
ghcr.io/YOUR_USERNAME/ktor-microservice:latest
ghcr.io/YOUR_USERNAME/ktor-microservice:main
ghcr.io/YOUR_USERNAME/ktor-microservice:develop  
ghcr.io/YOUR_USERNAME/ktor-microservice:v1.0.0
```

### Pulling Images
```bash
# Login to GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull latest
docker pull ghcr.io/YOUR_USERNAME/ktor-microservice:latest

# Pull specific version
docker pull ghcr.io/YOUR_USERNAME/ktor-microservice:v1.0.0
```

---

## 🎯 Usage Examples

### Running Tests Locally
```bash
./gradlew test
```

### Creating a Release
```bash
# Create and push tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# GitHub Actions will:
# 1. Build JAR
# 2. Create GitHub Release
# 3. Build Docker image
# 4. Tag as v1.0.0 and latest
```

### Manual Workflow Trigger
Go to `Actions` tab → Select workflow → `Run workflow`

---

## 📊 Monitoring

### View Workflow Runs
1. Go to `Actions` tab
2. Select workflow
3. View run details and logs

### Test Reports
- Automatically published as artifacts
- Available in workflow run summary

### Security Alerts
- View in `Security` tab → `Code scanning alerts`
- Trivy scans uploaded to Security tab

---

## 🔄 Workflow Status Badges

Add to your README.md:
```markdown
![CI/CD](https://github.com/YOUR_USERNAME/ktor-microservice/workflows/CI%2FCD%20Pipeline/badge.svg)
![Tests](https://github.com/YOUR_USERNAME/ktor-microservice/workflows/Tests/badge.svg)
![Security](https://github.com/YOUR_USERNAME/ktor-microservice/workflows/Code%20Quality/badge.svg)
```

---

## 🐛 Troubleshooting

### Tests Failing
- Check PostgreSQL service is running
- Verify database schema is created
- Check environment variables

### Docker Build Failing
- Verify Dockerfile syntax
- Check build context includes all files
- Review build logs in Actions tab

### Permission Denied
- Ensure `packages: write` permission
- Check GITHUB_TOKEN has correct scopes

---

## 📝 Best Practices

1. **Branch Protection**
   - Require status checks to pass
   - Require pull request reviews
   - Enable "Require branches to be up to date"

2. **Caching**
   - Workflows use Gradle caching
   - Docker layer caching enabled
   - Reduces build times significantly

3. **Security**
   - Dependabot keeps dependencies updated
   - Trivy scans for vulnerabilities
   - Security alerts enabled

4. **Testing**
   - Run tests on every push
   - Use database services for integration tests
   - Upload test results as artifacts

---

For more information, see [GitHub Actions Documentation](https://docs.github.com/en/actions)
