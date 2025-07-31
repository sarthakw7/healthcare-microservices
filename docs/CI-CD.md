# CI/CD Pipeline Documentation

## 🚀 Overview

Our CI/CD pipeline uses **GitHub Actions** to automatically build, test, and deploy the healthcare microservices platform.

## 🔄 Pipeline Stages

### 1. **Continuous Integration (CI)**

**Triggers:** Push to `main`/`develop`, Pull Requests to `main`

**Steps:**

1. **Test** - Run unit tests for all 5 microservices
2. **Build** - Create Docker images for each service
3. **Security Scan** - Check for vulnerabilities
4. **Code Quality** - Static analysis and coverage reports
5. **Integration Test** - End-to-end API testing

### 2. **Continuous Deployment (CD)**

**Triggers:** Push to `main` branch (after successful CI)

**Steps:**

1. **Deploy to LocalStack** - Simulate AWS deployment
2. **Smoke Tests** - Verify deployment health
3. **Container Registry** - Push images to GitHub Container Registry

## 🛠️ Workflows

| Workflow          | File                                  | Purpose                   |
| ----------------- | ------------------------------------- | ------------------------- |
| **Main CI/CD**    | `.github/workflows/ci-cd.yml`         | Build, test, deploy       |
| **Security Scan** | `.github/workflows/security-scan.yml` | Vulnerability scanning    |
| **Code Quality**  | `.github/workflows/code-quality.yml`  | Static analysis, coverage |

## 📊 Quality Gates

- ✅ **Unit Tests** must pass (>80% coverage)
- ✅ **Integration Tests** must pass
- ✅ **Security Scan** must show no high-severity vulnerabilities
- ✅ **Code Quality** checks must pass

## 🐳 Container Registry

Images are pushed to: `ghcr.io/sarthakw7/healthcare-microservices`

**Available Images:**

- `auth-service:latest`
- `patient-service:latest`
- `billing-service:latest`
- `analytics-service:latest`
- `api-gateway:latest`

## 🔧 Local Development

```bash
# Run the same checks locally
mvn clean test                    # Unit tests
mvn jacoco:report                 # Coverage report
mvn spotbugs:check               # Static analysis
mvn org.owasp:dependency-check-maven:check  # Security scan
```

## 🚀 Deployment

**Automatic:** Push to `main` triggers deployment to LocalStack

**Manual:** Use GitHub Actions UI to trigger deployment

## 📈 Monitoring

- **Build Status:** GitHub Actions tab
- **Test Reports:** Artifacts in workflow runs
- **Coverage Reports:** Generated in `target/site/jacoco/`
- **Security Reports:** Generated in `target/dependency-check-report.html`

## 🔒 Security

- **Secrets Management:** GitHub Secrets for sensitive data
- **Container Scanning:** Automatic vulnerability detection
- **Dependency Scanning:** OWASP dependency check
- **Code Analysis:** SpotBugs static analysis

## 🎯 Best Practices

1. **Branch Protection:** Require PR reviews and status checks
2. **Automated Testing:** Never merge without passing tests
3. **Security First:** Regular vulnerability scans
4. **Quality Gates:** Maintain code coverage above 80%
5. **Fast Feedback:** Pipeline completes in <10 minutes
