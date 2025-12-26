# GitHub Actions CI/CD Workflows

This directory contains all GitHub Actions workflows for the Factory Management Microservices System.

## Table of Contents

- [Overview](#overview)
- [Workflows](#workflows)
- [Setup Instructions](#setup-instructions)
- [Usage Guide](#usage-guide)
- [Secrets and Variables](#secrets-and-variables)
- [Deployment Process](#deployment-process)
- [Troubleshooting](#troubleshooting)

---

## Overview

The CI/CD pipeline is designed to:
- ✅ Automatically test and build all services
- ✅ Build and push Docker images to GitHub Container Registry
- ✅ Deploy to staging and production environments
- ✅ Run security scans and code quality checks
- ✅ Manage database migrations
- ✅ Create releases with artifacts

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     GitHub Repository                        │
│                                                              │
│  Push to develop  →  CI/CD Pipeline  →  Deploy to Staging  │
│  Push to main     →  CI/CD Pipeline  →  Deploy to Prod     │
│  Create tag       →  Release Pipeline →  GitHub Release     │
│  Manual trigger   →  Manual Deploy   →  Custom Deployment  │
└─────────────────────────────────────────────────────────────┘
```

---

## Workflows

### 1. **ci-cd.yml** - Main CI/CD Pipeline

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`

**Jobs:**
1. **Code Quality** - Runs linters (detekt, ktlint)
2. **Build and Test** - Builds and tests all services in parallel
3. **Build Docker Images** - Builds and pushes to GHCR
4. **Deploy to Staging** - Auto-deploys develop branch
5. **Deploy to Production** - Auto-deploys main branch
6. **Security Scan** - Scans Docker images with Trivy

**Flow:**
```
Push to develop → Code Quality → Build/Test → Docker Build → Deploy Staging
Push to main    → Code Quality → Build/Test → Docker Build → Deploy Production
```

### 2. **pr-checks.yml** - Pull Request Checks

**Triggers:**
- Pull requests to `main` or `develop`

**Jobs:**
1. **PR Validation** - Validates PR title format (semantic)
2. **Quick Build** - Compiles all services
3. **Code Quality** - Runs linters and static analysis
4. **Test Services** - Runs unit tests for all services
5. **Docker Build Check** - Verifies Docker images can be built
6. **Dependency Check** - Scans for vulnerable dependencies
7. **PR Status** - Final status check and comment

**Example PR Title:**
```
feat: add category filtering to inventory service
fix: resolve JWT token expiration issue
docs: update API documentation
```

### 3. **manual-deploy.yml** - Manual Deployment

**Triggers:**
- Manual workflow dispatch

**Inputs:**
- `environment` - staging or production
- `service` - all, auth-service, inventory-service, order-service, or telegram-bot-service
- `image_tag` - Docker image tag to deploy

**Use Cases:**
- Hotfix deployment
- Rollback to previous version
- Deploy specific service
- Test deployment process

**Example:**
```
Environment: production
Service: auth-service
Image Tag: main-abc123
```

### 4. **database-migration.yml** - Database Migration

**Triggers:**
- Manual workflow dispatch

**Inputs:**
- `environment` - staging or production
- `service` - all, auth-service, inventory-service, or order-service
- `action` - migrate, rollback, or seed
- `dry_run` - true (preview) or false (execute)

**Use Cases:**
- Run database migrations
- Rollback migrations
- Seed demo data
- Preview migration changes

**Safety Features:**
- Dry run mode by default
- Production backup before migration
- Validation checks

### 5. **release.yml** - Release Management

**Triggers:**
- Push tags matching `v*.*.*` (e.g., v1.0.0)
- Manual workflow dispatch

**Jobs:**
1. **Create Release** - Generates changelog and GitHub release
2. **Build Docker Images** - Builds and tags release images
3. **Notify Release** - Sends notifications

**Release Artifacts:**
- Fat JARs for all services
- Docker images with version tags
- Changelog
- Release notes

**Docker Tags:**
```
v1.2.3 → factory/auth-service:1.2.3
       → factory/auth-service:1.2
       → factory/auth-service:1
       → factory/auth-service:stable
```

---

## Setup Instructions

### 1. Enable GitHub Actions

1. Go to your repository settings
2. Navigate to Actions → General
3. Under "Actions permissions", select "Allow all actions and reusable workflows"
4. Save changes

### 2. Configure Secrets

Go to Settings → Secrets and variables → Actions

**Required Secrets:**

```bash
# GitHub Container Registry (automatically available)
GITHUB_TOKEN  # Automatically provided by GitHub

# Optional: Deployment Secrets
KUBECONFIG           # Kubernetes config for deployment
SLACK_WEBHOOK_URL    # Slack notifications
DOCKER_HUB_USERNAME  # If using Docker Hub
DOCKER_HUB_TOKEN     # If using Docker Hub
```

### 3. Configure Environments

Create two environments in Settings → Environments:

**Staging Environment:**
- Name: `staging`
- URL: `https://staging.factory.example.com`
- Protection rules: None (auto-deploy)

**Production Environment:**
- Name: `production`
- URL: `https://factory.example.com`
- Protection rules:
  - ✅ Required reviewers (at least 1)
  - ✅ Wait timer (optional: 5 minutes)

### 4. Enable GitHub Container Registry

1. Go to your profile → Settings → Developer settings
2. Personal access tokens → Tokens (classic)
3. Generate new token with `write:packages` permission
4. The workflow uses `GITHUB_TOKEN` automatically

---

## Usage Guide

### Deploying to Staging

**Automatic:**
1. Push to `develop` branch
2. CI/CD pipeline runs automatically
3. If tests pass, deploys to staging

**Manual:**
1. Go to Actions tab
2. Select "Manual Deployment"
3. Click "Run workflow"
4. Select:
   - Environment: staging
   - Service: all
   - Image tag: develop-latest
5. Click "Run workflow"

### Deploying to Production

**Automatic:**
1. Merge PR to `main` branch
2. CI/CD pipeline runs automatically
3. If tests pass, requires approval
4. After approval, deploys to production

**Manual:**
1. Go to Actions tab
2. Select "Manual Deployment"
3. Click "Run workflow"
4. Select:
   - Environment: production
   - Service: all
   - Image tag: main-abc123
5. Click "Run workflow"
6. Approve deployment when prompted

### Running Database Migrations

**Dry Run (Preview):**
1. Go to Actions tab
2. Select "Database Migration"
3. Click "Run workflow"
4. Select:
   - Environment: staging
   - Service: all
   - Action: migrate
   - Dry run: true ✅
5. Review output in logs

**Execute Migration:**
1. After dry run validation
2. Run workflow again with:
   - Dry run: false ❌
3. **Production migrations require backup first!**

### Creating a Release

**Option 1: Tag-based (Recommended)**
```bash
# Create and push a tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# Workflow triggers automatically
```

**Option 2: Manual**
1. Go to Actions tab
2. Select "Release"
3. Click "Run workflow"
4. Enter version (e.g., v1.0.0)
5. Click "Run workflow"

### Hotfix Deployment

1. Create hotfix branch from main
2. Make changes and push
3. Use Manual Deployment:
   - Build Docker image from hotfix branch
   - Note the image SHA
   - Deploy using that specific SHA
4. After verification, merge to main

---

## Secrets and Variables

### GitHub Actions Secrets

| Secret Name | Description | Required For |
|-------------|-------------|--------------|
| `GITHUB_TOKEN` | GitHub authentication | All workflows (auto) |
| `KUBECONFIG` | Kubernetes config | Deployments |
| `SLACK_WEBHOOK_URL` | Slack notifications | Notifications |
| `DB_BACKUP_BUCKET` | S3 bucket for backups | DB migrations |

### Environment Variables

Configure in workflow files:

```yaml
env:
  REGISTRY: ghcr.io
  IMAGE_PREFIX: ${{ github.repository_owner }}/factory
  JAVA_VERSION: '21'
  GRADLE_VERSION: '8.5'
```

---

## Deployment Process

### Complete Deployment Flow

```
┌──────────────┐
│ Code Change  │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ Push to Git  │
└──────┬───────┘
       │
       ▼
┌─────────────────────┐
│ CI/CD Workflow      │
│ - Code Quality      │
│ - Build & Test      │
│ - Docker Build      │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐     ┌──────────────┐
│ Deploy to Staging   │────▶│ Run Tests    │
└──────┬──────────────┘     └──────┬───────┘
       │                            │
       │                            ▼
       │                    ┌──────────────┐
       │                    │ Verified ✅   │
       │                    └──────────────┘
       ▼
┌─────────────────────┐
│ Approval Required   │
│ (Production only)   │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Deploy to Prod      │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Smoke Tests         │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Send Notification   │
└─────────────────────┘
```

### Rollback Process

**Immediate Rollback:**
```bash
# Use Manual Deployment
Environment: production
Service: all
Image Tag: v1.0.0  # Previous working version
```

**Database Rollback:**
```bash
# 1. Restore database backup
kubectl exec postgres-pod -- pg_restore backup.sql

# 2. Use Database Migration workflow
Action: rollback
```

---

## Troubleshooting

### Workflow Fails at Build Step

**Problem:** `./gradlew build` fails

**Solutions:**
1. Check Gradle cache:
   ```yaml
   - uses: actions/setup-java@v4
     with:
       cache: 'gradle'
   ```

2. Clean build locally:
   ```bash
   ./gradlew clean build
   ```

3. Check Java version matches (JDK 21)

### Docker Build Fails

**Problem:** `docker build` fails

**Solutions:**
1. Verify Dockerfile exists
2. Check build context:
   ```yaml
   context: .
   file: auth-service/Dockerfile
   ```

3. Test locally:
   ```bash
   docker build -t test -f auth-service/Dockerfile .
   ```

### Deployment Fails

**Problem:** Deployment step fails

**Solutions:**
1. Check environment secrets are set
2. Verify KUBECONFIG is valid
3. Test kubectl access:
   ```bash
   kubectl get pods
   ```

4. Check service health after deployment:
   ```bash
   kubectl get pods -l app=auth-service
   kubectl logs -l app=auth-service
   ```

### Image Not Found

**Problem:** `Error: image not found`

**Solutions:**
1. Verify image was pushed:
   - Go to Packages in GitHub
   - Check image exists with correct tag

2. Check image pull permissions:
   ```yaml
   - name: Log in to registry
     uses: docker/login-action@v3
   ```

3. Verify image name format:
   ```
   ghcr.io/username/factory/auth-service:tag
   ```

### Tests Fail in CI but Pass Locally

**Problem:** Tests pass locally but fail in GitHub Actions

**Solutions:**
1. Check environment differences:
   - Database availability
   - Timezone settings
   - File permissions

2. Add debug logging:
   ```yaml
   - name: Debug environment
     run: |
       env
       pwd
       ls -la
   ```

3. Use same JDK version locally

---

## Best Practices

### Branch Strategy

```
main (production)
  │
  ├── develop (staging)
  │     │
  │     ├── feature/add-inventory
  │     ├── feature/telegram-bot
  │     └── bugfix/auth-issue
  │
  └── hotfix/critical-bug
```

### Commit Messages

Follow conventional commits:

```
feat: add product filtering
fix: resolve JWT expiration issue
docs: update API documentation
test: add integration tests for orders
refactor: simplify auth service
perf: optimize database queries
ci: update deployment workflow
```

### Pull Request Workflow

1. Create feature branch
2. Make changes and commit
3. Push and create PR
4. Wait for CI checks to pass
5. Request review
6. Merge after approval

### Release Workflow

1. Update version in relevant files
2. Create changelog
3. Create and push tag
4. Workflow creates release automatically
5. Verify Docker images are tagged
6. Deploy to production

---

## Monitoring and Alerts

### Workflow Notifications

Add to workflow:

```yaml
- name: Notify on failure
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    text: 'Deployment failed!'
    webhook_url: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### Metrics to Monitor

- ✅ Build success rate
- ✅ Test pass rate
- ✅ Deployment frequency
- ✅ Time to deploy
- ✅ Rollback frequency

---

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [Kubernetes Deployment Guide](../k8s/README.md)
- [Project README](../../README.md)

---

## Support

For issues or questions:
1. Check this README
2. Review workflow logs in Actions tab
3. Check [Troubleshooting](#troubleshooting) section
4. Create issue in repository

---

**Last Updated:** December 26, 2025
**Maintained By:** DevOps Team
