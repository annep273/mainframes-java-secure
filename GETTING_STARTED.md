# Getting Started Guide

Welcome! This guide will help you get the Mainframes Java application up and running in just 5 minutes locally, or deployed to s390x OpenShift in under an hour.

## Table of Contents
- [Quick Start (Local)](#quick-start-local)
- [Full Deployment (OpenShift)](#full-deployment-openshift)
- [Verifying Your Setup](#verifying-your-setup)
- [Next Steps](#next-steps)

---

## Quick Start (Local)

### Prerequisites
- Java 17+ installed
- Maven 3.9+ installed
- 10 minutes of your time

### Step 1: Verify Prerequisites

```bash
# Check Java version
java -version
# Should show: openjdk version "17" or higher

# Check Maven version
mvn -version
# Should show: Apache Maven 3.9 or higher
```

### Step 2: Clone or Navigate to Repository

```bash
cd /path/to/Mainframes_java
```

### Step 3: Use Quick Start Script (Recommended)

```bash
# Make script executable (already done)
chmod +x quickstart.sh

# Run the script
./quickstart.sh
```

**Choose option 7**: Full local setup (build + test + run)

The script will:
1. ✅ Build the application
2. ✅ Run all tests
3. ✅ Start the application on http://localhost:8080

### Step 4: Test Your Local Application

Open a new terminal and try these commands:

```bash
# Test health endpoint
curl http://localhost:8080/actuator/health

# Expected output:
# {"status":"UP"}

# Test custom health endpoint
curl http://localhost:8080/api/v1/health

# Create a message
curl -X POST http://localhost:8080/api/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"content":"Hello from s390x!","author":"Developer"}'

# Get all messages
curl http://localhost:8080/api/v1/messages

# Open Swagger UI in browser
open http://localhost:8080/swagger-ui.html
```

### 🎉 Congratulations!

Your application is running locally! You can now:
- Explore the API at http://localhost:8080/swagger-ui.html
- View metrics at http://localhost:8080/actuator/metrics
- Check health at http://localhost:8080/actuator/health

**Press Ctrl+C** in the first terminal to stop the application.

---

## Full Deployment (OpenShift)

This section covers deploying to an s390x OpenShift cluster via GitHub Actions.

### Time Required
- Setup: 30 minutes
- First deployment: 20-30 minutes (s390x build is slow with QEMU)
- Subsequent deployments: 20-30 minutes

### Prerequisites Checklist

Before starting, ensure you have:

- [ ] GitHub account
- [ ] OpenShift cluster with s390x nodes
- [ ] OpenShift CLI (`oc`) installed
- [ ] Helm 3.13+ installed
- [ ] Container registry account (Quay.io recommended)
- [ ] Cluster admin access or equivalent permissions

### Phase 1: GitHub Setup (10 minutes)

#### 1.1 Create GitHub Repository

```bash
# Initialize git (if not already done)
cd /path/to/Mainframes_java
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit: Complete s390x Java application"

# Create repo on GitHub (via web UI or gh CLI)
gh repo create mainframes-java --public --source=. --remote=origin

# Push code
git push -u origin main
```

#### 1.2 Configure GitHub Secrets

Navigate to: **Settings → Secrets and variables → Actions → New repository secret**

Add these secrets:

| Secret Name | Description | How to Get |
|-------------|-------------|------------|
| `OPENSHIFT_SERVER` | OpenShift API URL | `oc whoami --show-server` |
| `OPENSHIFT_TOKEN` | Service account token | See below |
| `OPENSHIFT_NAMESPACE` | Target namespace | e.g., `mainframes-app` |
| `QUAY_USERNAME` | Registry username | Your Quay.io username |
| `QUAY_PASSWORD` | Registry password/token | Your Quay.io password |

**Getting OpenShift Token:**

```bash
# Login to OpenShift
oc login --server=https://api.your-cluster.com:6443

# Create service account
oc create serviceaccount github-deployer -n mainframes-app

# Grant admin permissions to the service account
oc adm policy add-role-to-user admin \
  system:serviceaccount:mainframes-app:github-deployer \
  -n mainframes-app

# Get the token
oc serviceaccounts get-token github-deployer -n mainframes-app
# Copy this token to OPENSHIFT_TOKEN secret
```

### Phase 2: Container Registry Setup (5 minutes)

#### 2.1 Create Quay.io Account
1. Visit https://quay.io/signin/
2. Sign up or login
3. Create new repository: `mainframes-java-app`
4. Make it public or configure robot account for private access

#### 2.2 Test Registry Access

```bash
# Login to registry
docker login quay.io -u your-username

# Verify you can push (optional)
docker tag hello-world:latest quay.io/your-username/test:latest
docker push quay.io/your-username/test:latest
docker rmi quay.io/your-username/test:latest
```

### Phase 3: OpenShift Preparation (10 minutes)

#### 3.1 Verify s390x Nodes

```bash
# Login to OpenShift
oc login --server=https://api.your-cluster.com:6443 --token=your-token

# Check for s390x nodes
oc get nodes -l kubernetes.io/arch=s390x

# Expected output:
# NAME            STATUS   ROLES    AGE   VERSION
# s390x-node-1   Ready    worker   30d   v1.25.4
# s390x-node-2   Ready    worker   30d   v1.25.4
```

**No s390x nodes?** This application requires s390x architecture nodes. Contact your cluster administrator.

#### 3.2 Create Namespace

```bash
# Create namespace
oc new-project mainframes-app

# Verify it's created and active
oc project
# You are using project "mainframes-app"
```

#### 3.3 Create Image Pull Secret (if using private registry)

```bash
oc create secret docker-registry quay-pull-secret \
  --docker-server=quay.io \
  --docker-username=your-username \
  --docker-password=your-password \
  -n mainframes-app

# Verify
oc get secrets -n mainframes-app
```

### Phase 4: First Deployment (25-35 minutes)

#### 4.1 Trigger GitHub Actions

```bash
# Make a small change to trigger CI/CD
echo "# Deployed $(date)" >> README.md
git add README.md
git commit -m "trigger: Initial deployment"
git push origin main
```

#### 4.2 Monitor Build Progress

1. Go to GitHub → Your Repository → Actions tab
2. Click on the latest workflow run
3. Watch the progress:
   - ✅ **CI Workflow** (~3-5 min)
     - Checkout, test, build
   - ⏳ **CD Workflow** (~20-30 min)
     - QEMU setup
     - s390x build (THIS IS SLOW - it's normal!)
     - Push to registry
     - Deploy to OpenShift
     - Smoke tests

**Expected timeline:**
- 0-5 min: CI tests complete
- 5-10 min: Docker build starts
- 10-25 min: s390x build in progress (be patient!)
- 25-27 min: Push to registry
- 27-30 min: Deploy to OpenShift
- 30 min: Smoke tests and completion

#### 4.3 Monitor OpenShift Deployment

While GitHub Actions is running, monitor OpenShift:

```bash
# Watch pods being created
watch oc get pods -n mainframes-app

# Watch events
oc get events -n mainframes-app --sort-by='.lastTimestamp'

# Check deployment status
oc rollout status deployment/mainframes-java-app -n mainframes-app
```

### Phase 5: Verification (5 minutes)

#### 5.1 Get Application URL

```bash
# Get the route
export ROUTE=$(oc get route mainframes-java-app -n mainframes-app -o jsonpath='{.spec.host}')
echo "Application URL: https://$ROUTE"
```

#### 5.2 Test Endpoints

```bash
# Test health
curl -k https://$ROUTE/actuator/health

# Test custom health
curl -k https://$ROUTE/api/v1/health

# Create a message
curl -k -X POST https://$ROUTE/api/v1/messages \
  -H "Content-Type: application/json" \
  -d '{"content":"Deployed to s390x OpenShift!","author":"Admin"}'

# Get messages
curl -k https://$ROUTE/api/v1/messages

# Check metrics
curl -k https://$ROUTE/actuator/prometheus
```

#### 5.3 Verify s390x Architecture

```bash
# Get pod name
POD=$(oc get pod -l app=mainframes-java-app -n mainframes-app -o jsonpath='{.items[0].metadata.name}')

# Check architecture
oc exec $POD -n mainframes-app -- uname -m
# Expected output: s390x

# Check Java version
oc exec $POD -n mainframes-app -- java -version
```

#### 5.4 Open Swagger UI

```bash
# Open in browser
open https://$ROUTE/swagger-ui.html
# Or visit manually in your browser
```

### 🚀 Deployment Complete!

Your application is now running on s390x OpenShift! 

---

## Verifying Your Setup

### Health Checks

```bash
# Liveness probe
curl -k https://$ROUTE/actuator/health/liveness

# Readiness probe
curl -k https://$ROUTE/actuator/health/readiness

# All health checks
curl -k https://$ROUTE/actuator/health | jq .
```

### Resource Usage

```bash
# Check pod resources
oc adm top pods -n mainframes-app

# Expected output:
# NAME                                   CPU(cores)   MEMORY(bytes)
# mainframes-java-app-xxxx-yyyy          50m          350Mi
```

### Logs

```bash
# View logs
oc logs -f deployment/mainframes-java-app -n mainframes-app

# View recent logs
oc logs deployment/mainframes-java-app -n mainframes-app --tail=100
```

### Horizontal Pod Autoscaler

```bash
# Check HPA status
oc get hpa -n mainframes-app

# Expected output:
# NAME                  REFERENCE                        TARGETS         MINPODS   MAXPODS   REPLICAS
# mainframes-java-app   Deployment/mainframes-java-app   10%/80%, 5%/80%   2         10        2
```

### Metrics & Monitoring

```bash
# Check metrics endpoint
curl -k https://$ROUTE/actuator/metrics

# Check Prometheus metrics
curl -k https://$ROUTE/actuator/prometheus | head -20

# If Prometheus is installed, verify ServiceMonitor
oc get servicemonitor -n mainframes-app
```

---

## Next Steps

### 1. Explore the API

- **Swagger UI**: https://$ROUTE/swagger-ui.html
- **API Docs**: https://$ROUTE/v3/api-docs
- Try all the endpoints interactively

### 2. Customize Configuration

Edit [helm/mainframes-java-app/values.yaml](helm/mainframes-java-app/values.yaml):

```yaml
# Change replica count
replicaCount: 3

# Adjust resource limits
resources:
  limits:
    memory: "1Gi"
    cpu: "1000m"
  requests:
    memory: "300Mi"
    cpu: "150m"

# Enable/disable features
hpa:
  enabled: true
  minReplicas: 3
  maxReplicas: 20
```

Redeploy:
```bash
git add helm/mainframes-java-app/values.yaml
git commit -m "config: Update resource limits"
git push origin main
```

### 3. Add Database Integration

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md#future-enhancements) for:
- PostgreSQL integration
- MongoDB setup
- Redis caching

### 4. Implement Authentication

Options:
- OAuth2/OIDC
- JWT tokens
- OpenShift OAuth integration

### 5. Add Monitoring Dashboards

- Set up Grafana dashboards
- Configure alerts in AlertManager
- Integrate with PagerDuty/Slack

### 6. Performance Testing

```bash
# Install Apache Bench
brew install apache-bench  # macOS
sudo apt-get install apache2-utils  # Linux

# Run load test
ab -n 10000 -c 100 https://$ROUTE/api/v1/health

# Watch HPA scale up
watch oc get hpa -n mainframes-app
```

### 7. Explore CI/CD Customization

- Add staging environment
- Implement blue-green deployments
- Add manual approval gates
- Configure Slack notifications

---

## Common Issues

### Build Takes Forever
**This is normal!** s390x builds with QEMU emulation take 15-30 minutes. Be patient.

**Solution**: Consider self-hosted s390x runners for 5x faster builds.

### Pod in CrashLoopBackOff
```bash
# Check logs
oc logs <pod-name> -n mainframes-app

# Check events
oc describe pod <pod-name> -n mainframes-app
```

**Common causes**:
- Out of memory → Increase limits
- Failed health checks → Check probe configuration
- Image pull errors → Verify registry credentials

### Cannot Access Route
```bash
# Verify route exists
oc get route -n mainframes-app

# Check service endpoints
oc get endpoints -n mainframes-app
```

**Common causes**:
- Pods not ready → Wait for readiness probes
- TLS issues → Use `-k` flag with curl
- Network policy → Check firewall rules

### Image Pull Errors
```bash
# Check secret exists
oc get secret quay-pull-secret -n mainframes-app

# Verify secret is linked
oc describe serviceaccount default -n mainframes-app
```

---

## Getting Help

### Documentation
- 📖 [README.md](README.md) - Project overview
- 📋 [CHECKLIST.md](CHECKLIST.md) - Deployment checklist
- 🔧 [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) - Detailed troubleshooting
- 🏗️ [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - Architecture guide
- 📊 [docs/DIAGRAMS.md](docs/DIAGRAMS.md) - Visual diagrams

### Commands Reference

```bash
# Quick reference
./quickstart.sh              # Interactive menu
make help                    # Show all make targets
oc get all -n mainframes-app # Show all resources
helm status mainframes-java-app -n mainframes-app  # Deployment status
```

### Support Channels
- 🐛 GitHub Issues: Report bugs and request features
- 💬 Slack: #mainframes-support (if applicable)
- 📧 Email: support@mainframes.com

---

## Summary

You've successfully:
- ✅ Set up local development environment
- ✅ Deployed to s390x OpenShift cluster
- ✅ Configured CI/CD with GitHub Actions
- ✅ Verified the application is running
- ✅ Tested all endpoints

**Congratulations!** 🎉

You now have a production-ready Java application running on s390x OpenShift with full CI/CD automation.

---

**Document Version**: 1.0.0  
**Last Updated**: January 2026  
**Maintained By**: Mainframes Team
