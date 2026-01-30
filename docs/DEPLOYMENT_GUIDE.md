# Step-by-Step Deployment Guide for s390x OpenShift

This comprehensive guide covers everything you need to build and deploy the Mainframes Java Application to an s390x OpenShift cluster using GitHub Actions.

## Table of Contents

1. [Environment Setup](#1-environment-setup)
2. [Repository Configuration](#2-repository-configuration)
3. [Container Registry Setup](#3-container-registry-setup)
4. [OpenShift Cluster Preparation](#4-openshift-cluster-preparation)
5. [GitHub Actions Configuration](#5-github-actions-configuration)
6. [First Deployment](#6-first-deployment)
7. [Verification and Testing](#7-verification-and-testing)
8. [Common Failure Scenarios](#8-common-failure-scenarios)
9. [Best Practices](#9-best-practices)
10. [Rollback Procedures](#10-rollback-procedures)

---

## 1. Environment Setup

### Prerequisites Checklist

- [ ] Java 17+ installed locally
- [ ] Maven 3.9+ installed
- [ ] Docker with Buildx support
- [ ] Git installed
- [ ] OpenShift CLI (`oc`) installed
- [ ] Helm 3.13+ installed
- [ ] Access to s390x OpenShift cluster
- [ ] Container registry account (Quay.io recommended)
- [ ] GitHub account with Actions enabled

### Install Required Tools

#### On macOS:
```bash
# Install Java
brew install openjdk@17

# Install Maven
brew install maven

# Install Docker Desktop
# Download from https://www.docker.com/products/docker-desktop

# Install OpenShift CLI
brew install openshift-cli

# Install Helm
brew install helm
```

#### On Linux (Ubuntu/Debian):
```bash
# Install Java
sudo apt update
sudo apt install openjdk-17-jdk

# Install Maven
sudo apt install maven

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install OpenShift CLI
curl -LO https://mirror.openshift.com/pub/openshift-v4/clients/oc/latest/linux/oc.tar.gz
tar xvf oc.tar.gz
sudo mv oc /usr/local/bin/
chmod +x /usr/local/bin/oc

# Install Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

### Verify Installations

```bash
# Check versions
java -version
mvn -version
docker --version
oc version
helm version
```

---

## 2. Repository Configuration

### Step 2.1: Clone the Repository

```bash
git clone https://github.com/yourusername/mainframes-java.git
cd mainframes-java
```

### Step 2.2: Verify Project Structure

```bash
# Verify all files are present
ls -la

# Expected output should include:
# - pom.xml
# - Dockerfile
# - Dockerfile.ubi
# - .github/workflows/
# - helm/
# - src/
```

### Step 2.3: Test Local Build

```bash
# Build the application
mvn clean package

# Verify JAR was created
ls -lh target/*.jar

# Run tests
mvn test

# Expected: All tests should pass
```

### Step 2.4: Update Application Configuration

Edit `src/main/resources/application.yml` if needed:

```yaml
spring:
  application:
    name: mainframes-java-app  # Change if desired

server:
  port: 8080  # Change if needed
```

---

## 3. Container Registry Setup

### Option A: Quay.io (Recommended)

#### Step 3.1: Create Quay.io Account

1. Go to https://quay.io
2. Sign up for a free account
3. Verify your email

#### Step 3.2: Create Repository

1. Click "Create New Repository"
2. Repository name: `mainframes-java-app`
3. Visibility: Private or Public
4. Click "Create Public Repository"

#### Step 3.3: Generate Robot Account

1. Go to Account Settings → Robot Accounts
2. Click "Create Robot Account"
3. Name: `github_actions_robot`
4. Give it write permissions to your repository
5. Copy the credentials (username and password)

#### Step 3.4: Test Registry Access

```bash
# Login to Quay
docker login quay.io
# Enter your username and password

# Tag a test image
docker tag hello-world quay.io/your-org/test:latest

# Push test image
docker push quay.io/your-org/test:latest

# Clean up
docker rmi quay.io/your-org/test:latest
```

### Option B: Docker Hub

```bash
# Login to Docker Hub
docker login

# Create repository via web interface or CLI
# Use format: dockerhub-username/mainframes-java-app
```

---

## 4. OpenShift Cluster Preparation

### Step 4.1: Access OpenShift Cluster

```bash
# Login to OpenShift cluster
oc login --server=https://api.your-cluster.com:6443

# Or use token authentication
oc login --token=<your-token> --server=https://api.your-cluster.com:6443
```

### Step 4.2: Verify s390x Nodes Available

```bash
# List all nodes with architecture
oc get nodes -o wide

# Filter for s390x nodes
oc get nodes -l kubernetes.io/arch=s390x

# Expected output should show at least one s390x node
```

### Step 4.3: Create Project/Namespace

```bash
# Create new project
oc new-project mainframes-app

# Or use existing project
oc project mainframes-app

# Verify current project
oc project
```

### Step 4.4: Create Service Account (Optional but Recommended)

```bash
# Create service account for the application
oc create serviceaccount mainframes-sa -n mainframes-app

# Grant necessary permissions
oc adm policy add-scc-to-user anyuid -z mainframes-sa -n mainframes-app
```

### Step 4.5: Get OpenShift Token for GitHub Actions

```bash
# Get your current token
oc whoami -t

# Save this token - you'll need it for GitHub Secrets

# Or create a service account token (recommended for CI/CD)
oc create serviceaccount github-deployer -n mainframes-app

# Grant cluster-admin role (or more restrictive role)
oc adm policy add-cluster-role-to-user cluster-admin -z github-deployer -n mainframes-app

# Get the token
oc serviceaccounts get-token github-deployer -n mainframes-app
```

### Step 4.6: Configure OpenShift Route Hostname

```bash
# Get cluster apps domain
oc get ingresses.config.openshift.io cluster -o jsonpath='{.spec.domain}'

# Your route will be: mainframes-app.<cluster-apps-domain>
# Example: mainframes-app.apps.mycluster.example.com
```

---

## 5. GitHub Actions Configuration

### Step 5.1: Fork/Clone Repository to Your GitHub

```bash
# If you haven't already, push code to your GitHub
git remote add origin https://github.com/yourusername/mainframes-java.git
git branch -M main
git push -u origin main
```

### Step 5.2: Configure GitHub Secrets

Go to your repository on GitHub:
1. Click on "Settings"
2. Navigate to "Secrets and variables" → "Actions"
3. Click "New repository secret"

Add the following secrets:

| Secret Name | Description | Example Value |
|-------------|-------------|---------------|
| `REGISTRY_NAMESPACE` | Your container registry org/namespace | `your-org` |
| `REGISTRY_USERNAME` | Registry username or robot account | `your-org+robot` |
| `REGISTRY_PASSWORD` | Registry password or robot token | `ABCD1234...` |
| `OPENSHIFT_SERVER` | OpenShift API server URL | `https://api.cluster.com:6443` |
| `OPENSHIFT_TOKEN` | OpenShift authentication token | `sha256~abcd...` |
| `OPENSHIFT_NAMESPACE` | OpenShift project/namespace | `mainframes-app` |
| `OPENSHIFT_ROUTE_HOST` | Application route hostname | `mainframes-app.apps.cluster.com` |

### Step 5.3: Verify Workflow Files

Check that workflow files exist:

```bash
ls -la .github/workflows/
# Should see:
# - ci.yml
# - cd.yml
# - build-ubi.yml
```

### Step 5.4: Enable GitHub Actions

1. Go to repository "Actions" tab
2. If prompted, click "I understand my workflows, go ahead and enable them"

---

## 6. First Deployment

### Step 6.1: Trigger Initial Build

```bash
# Make a small change to trigger workflow
echo "# v1.0.0" >> README.md
git add README.md
git commit -m "Trigger initial deployment"
git push origin main
```

### Step 6.2: Monitor GitHub Actions

1. Go to "Actions" tab in GitHub
2. Click on the running workflow
3. Watch the build progress

**Expected workflow stages:**

```
CI Workflow:
├── Test (runs tests)
├── Build (creates JAR)
└── Code Quality (analyzes code)

CD Workflow:
├── Build and Push s390x Image
│   ├── Checkout code
│   ├── Build with Maven
│   ├── Set up QEMU
│   ├── Set up Docker Buildx
│   ├── Login to registry
│   ├── Build multi-arch image
│   └── Scan for vulnerabilities
└── Deploy to OpenShift
    ├── Install OpenShift CLI
    ├── Login to OpenShift
    ├── Create image pull secret
    ├── Deploy with Helm
    ├── Verify deployment
    └── Run smoke tests
```

### Step 6.3: Monitor Build Time

**Typical build times:**

- Maven build: 2-5 minutes
- Docker image build (s390x with QEMU): 10-30 minutes
- Helm deployment: 2-5 minutes
- Total: 15-40 minutes

**Note:** s390x builds using QEMU emulation are significantly slower than native builds.

---

## 7. Verification and Testing

### Step 7.1: Verify Pods are Running

```bash
# Check pod status
oc get pods -n mainframes-app

# Expected output:
# NAME                                    READY   STATUS    RESTARTS   AGE
# mainframes-java-app-xxxxx-xxxxx        1/1     Running   0          2m

# Get detailed pod information
oc describe pod <pod-name> -n mainframes-app
```

### Step 7.2: Verify Pod Architecture

```bash
# Get pod name
POD=$(oc get pods -n mainframes-app -l app.kubernetes.io/name=mainframes-java-app -o jsonpath='{.items[0].metadata.name}')

# Check architecture
oc exec -n mainframes-app $POD -- uname -m

# Expected output: s390x
```

### Step 7.3: Check Application Logs

```bash
# View application logs
oc logs -f $POD -n mainframes-app

# Expected to see Spring Boot startup logs:
# Starting MainframesApplication...
# Started MainframesApplication in X.XXX seconds
```

### Step 7.4: Test Health Endpoints

```bash
# Get route URL
ROUTE=$(oc get route mainframes-java-app -n mainframes-app -o jsonpath='{.spec.host}')
echo "Application URL: https://$ROUTE"

# Test health endpoint
curl -k https://$ROUTE/actuator/health

# Expected output:
# {"status":"UP"}

# Test custom health endpoint
curl -k https://$ROUTE/api/v1/health

# Expected: JSON response with system information
```

### Step 7.5: Test API Endpoints

```bash
# Get all messages
curl -k https://$ROUTE/api/v1/messages

# Create a message
curl -k -X POST https://$ROUTE/api/v1/messages \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Test message from s390x",
    "author": "Test User"
  }'

# Verify message was created
curl -k https://$ROUTE/api/v1/messages
```

### Step 7.6: Access Swagger Documentation

```bash
# Open in browser
open https://$ROUTE/swagger-ui.html

# Or get the URL
echo "Swagger UI: https://$ROUTE/swagger-ui.html"
```

### Step 7.7: Check Prometheus Metrics

```bash
# Get metrics
curl -k https://$ROUTE/actuator/prometheus

# Should see metrics like:
# jvm_memory_used_bytes
# http_server_requests_seconds
# system_cpu_usage
```

---

## 8. Common Failure Scenarios

### Scenario 1: Build Timeout in GitHub Actions

**Symptoms:**
```
Error: The operation was canceled.
Build time exceeded maximum allowed (360 minutes)
```

**Causes:**
- QEMU emulation is slow for large applications
- Insufficient GitHub Actions resources
- Large dependencies being downloaded

**Solutions:**

1. **Enable Docker Layer Caching:**
```yaml
# In .github/workflows/cd.yml
- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    cache-from: type=registry,ref=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:buildcache
    cache-to: type=registry,ref=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:buildcache,mode=max
```

2. **Use Multi-stage Build Efficiently:**
```dockerfile
# Optimize Dockerfile
# Cache Maven dependencies separately
COPY pom.xml .
RUN mvn dependency:go-offline

# Then copy source and build
COPY src ./src
RUN mvn package -DskipTests
```

3. **Use Self-hosted s390x Runner:**
See [Setting up Self-hosted Runner](#setting-up-self-hosted-s390x-runner)

### Scenario 2: Image Pull Error

**Symptoms:**
```
Failed to pull image "quay.io/org/mainframes-java-app:latest": 
ErrImagePull / ImagePullBackOff
```

**Causes:**
- Image pull secret not created or incorrect
- Wrong image name or tag
- Registry authentication failed

**Solutions:**

```bash
# Verify image exists in registry
docker pull quay.io/your-org/mainframes-java-app:latest

# Delete and recreate image pull secret
oc delete secret quay-pull-secret -n mainframes-app

oc create secret docker-registry quay-pull-secret \
  --docker-server=quay.io \
  --docker-username=<username> \
  --docker-password=<password> \
  -n mainframes-app

# Link secret to service account
oc secrets link mainframes-sa quay-pull-secret --for=pull -n mainframes-app

# Update Helm values
helm upgrade mainframes-java-app ./helm/mainframes-java-app \
  --set imagePullSecrets[0].name=quay-pull-secret \
  --reuse-values
```

### Scenario 3: CrashLoopBackOff

**Symptoms:**
```
NAME                               READY   STATUS             RESTARTS   AGE
mainframes-java-app-xxxxx-xxxxx   0/1     CrashLoopBackOff   5          3m
```

**Diagnosis:**

```bash
# Check pod logs
oc logs <pod-name> -n mainframes-app

# Check pod events
oc describe pod <pod-name> -n mainframes-app

# Check previous container logs if available
oc logs <pod-name> -n mainframes-app --previous
```

**Common Causes & Solutions:**

1. **Insufficient Memory:**
```bash
# Increase memory limits
helm upgrade mainframes-java-app ./helm/mainframes-java-app \
  --set resources.limits.memory=2Gi \
  --set resources.requests.memory=1Gi
```

2. **Java Heap Size Issues:**
```bash
# Adjust JVM options
helm upgrade mainframes-java-app ./helm/mainframes-java-app \
  --set env[0].name=JAVA_OPTS \
  --set env[0].value="-XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0"
```

3. **Port Already in Use:**
```bash
# Check if port 8080 is the issue
oc logs <pod-name> | grep "Port 8080"

# Change port if needed
helm upgrade mainframes-java-app ./helm/mainframes-java-app \
  --set service.port=8081 \
  --set service.targetPort=8081
```

### Scenario 4: Slow s390x Node Scheduling

**Symptoms:**
```
Pod stuck in "Pending" state
Events: 0/3 nodes are available: 3 node(s) didn't match Pod's node affinity/selector
```

**Diagnosis:**

```bash
# Check if s390x nodes exist
oc get nodes -l kubernetes.io/arch=s390x

# Check node resources
oc describe node <s390x-node-name>
```

**Solutions:**

1. **Verify Node Selector:**
```bash
# Check current pod spec
oc get pod <pod-name> -o yaml | grep -A 2 nodeSelector

# Should show:
# nodeSelector:
#   kubernetes.io/arch: s390x
```

2. **Add Tolerations if Needed:**
```yaml
# In values.yaml
tolerations:
  - key: "arch"
    operator: "Equal"
    value: "s390x"
    effect: "NoSchedule"
```

3. **Check Resource Availability:**
```bash
# See available resources on s390x nodes
oc describe nodes -l kubernetes.io/arch=s390x | grep -A 5 "Allocated resources"
```

### Scenario 5: Health Check Failures

**Symptoms:**
```
Liveness probe failed: HTTP probe failed with statuscode: 503
Readiness probe failed: Get "http://10.x.x.x:8080/actuator/health/readiness": dial tcp 10.x.x.x:8080: connect: connection refused
```

**Solutions:**

1. **Increase Initial Delay:**
```yaml
# In values.yaml
livenessProbe:
  initialDelaySeconds: 90  # Increase from 60
  periodSeconds: 10
  
readinessProbe:
  initialDelaySeconds: 60  # Increase from 30
  periodSeconds: 10
```

2. **Check Application Startup Time:**
```bash
# Monitor application logs for startup time
oc logs -f <pod-name> | grep "Started MainframesApplication"
```

3. **Verify Endpoints Exist:**
```bash
# Port-forward to pod
oc port-forward <pod-name> 8080:8080 -n mainframes-app

# Test locally
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

### Scenario 6: Route/Ingress Not Accessible

**Symptoms:**
```
Cannot access https://mainframes-app.apps.cluster.com
Connection timeout or DNS resolution failure
```

**Solutions:**

1. **Verify Route Exists:**
```bash
# Check route
oc get route mainframes-java-app -n mainframes-app

# Get detailed route info
oc describe route mainframes-java-app -n mainframes-app
```

2. **Check Service:**
```bash
# Verify service endpoints
oc get svc mainframes-java-app -n mainframes-app
oc get endpoints mainframes-java-app -n mainframes-app

# Service should have endpoints listed
```

3. **Test from Inside Cluster:**
```bash
# Create test pod
oc run test --image=curlimages/curl --rm -it -- /bin/sh

# Inside pod, test service
curl http://mainframes-java-app.mainframes-app.svc:8080/actuator/health
```

4. **Check TLS Configuration:**
```bash
# If TLS issues, try without TLS first
oc patch route mainframes-java-app -n mainframes-app --type=json \
  -p='[{"op": "remove", "path": "/spec/tls"}]'
```

### Scenario 7: Helm Release Failed

**Symptoms:**
```
Error: INSTALLATION FAILED: unable to build kubernetes objects from release manifest
Error: UPGRADE FAILED: another operation (install/upgrade/rollback) is in progress
```

**Solutions:**

1. **Check Helm Release Status:**
```bash
# List releases
helm list -n mainframes-app

# Get release history
helm history mainframes-java-app -n mainframes-app

# Check for stuck release
kubectl get secret -n mainframes-app | grep mainframes-java-app
```

2. **Clean Up Failed Release:**
```bash
# Uninstall release
helm uninstall mainframes-java-app -n mainframes-app

# Wait a moment, then reinstall
helm install mainframes-java-app ./helm/mainframes-java-app \
  --set image.repository=quay.io/your-org/mainframes-java-app \
  --set image.tag=latest
```

3. **Force Upgrade:**
```bash
# If stuck in pending
helm upgrade mainframes-java-app ./helm/mainframes-java-app --force
```

---

## 9. Best Practices

### 9.1 Build Optimization

**Use BuildKit Cache:**
```yaml
# In CD workflow
cache-from: type=registry,ref=quay.io/org/mainframes-java-app:buildcache
cache-to: type=registry,ref=quay.io/org/mainframes-java-app:buildcache,mode=max
```

**Optimize Dockerfile:**
```dockerfile
# Bad: Copies everything, breaks cache
COPY . .
RUN mvn package

# Good: Copy dependencies first
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests
```

### 9.2 Resource Management

**Set Appropriate Resources:**
```yaml
resources:
  requests:
    cpu: 500m      # Guaranteed CPU
    memory: 512Mi  # Guaranteed memory
  limits:
    cpu: 1000m     # Maximum CPU
    memory: 1Gi    # Maximum memory
```

**Enable HPA (Horizontal Pod Autoscaler):**
```yaml
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
```

### 9.3 Security Best Practices

**1. Use Non-Root User:**
```dockerfile
# In Dockerfile
USER 1001
```

**2. Read-Only Root Filesystem:**
```yaml
securityContext:
  readOnlyRootFilesystem: true
  runAsNonRoot: true
  runAsUser: 1001
```

**3. Minimize Image Size:**
```dockerfile
# Use minimal base images
FROM eclipse-temurin:17-jre-alpine

# Remove unnecessary files
RUN rm -rf /var/cache/apk/*
```

**4. Scan Images:**
```bash
# Use Trivy or similar
docker scan quay.io/org/mainframes-java-app:latest
```

### 9.4 Monitoring and Observability

**Enable Prometheus ServiceMonitor:**
```yaml
monitoring:
  serviceMonitor:
    enabled: true
    interval: 30s
    path: /actuator/prometheus
```

**Configure Logging:**
```yaml
logging:
  level:
    root: INFO
    com.mainframes: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### 9.5 High Availability

**Pod Disruption Budget:**
```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: mainframes-java-app-pdb
spec:
  minAvailable: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: mainframes-java-app
```

**Anti-Affinity Rules:**
```yaml
affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      - labelSelector:
          matchExpressions:
            - key: app.kubernetes.io/name
              operator: In
              values:
                - mainframes-java-app
        topologyKey: kubernetes.io/hostname
```

### 9.6 CI/CD Best Practices

**1. Use Workflow Environments:**
```yaml
environment:
  name: production
  url: https://mainframes-app.apps.cluster.com
```

**2. Implement Approval Gates:**
```yaml
environment:
  name: production
jobs:
  deploy:
    environment:
      name: production
      # Requires manual approval
```

**3. Tag Images Properly:**
```yaml
tags: |
  type=ref,event=branch
  type=semver,pattern={{version}}
  type=sha,prefix={{branch}}-
```

**4. Run Smoke Tests:**
```bash
# In CD workflow
- name: Run smoke tests
  run: |
    curl -f https://$ROUTE/actuator/health || exit 1
    curl -f https://$ROUTE/api/v1/health || exit 1
```

---

## 10. Rollback Procedures

### 10.1 Helm Rollback

**List Release History:**
```bash
helm history mainframes-java-app -n mainframes-app
```

**Rollback to Previous Version:**
```bash
helm rollback mainframes-java-app -n mainframes-app
```

**Rollback to Specific Revision:**
```bash
helm rollback mainframes-java-app 3 -n mainframes-app
```

### 10.2 OpenShift Deployment Rollback

**View Deployment History:**
```bash
oc rollout history deployment/mainframes-java-app -n mainframes-app
```

**Rollback Deployment:**
```bash
oc rollout undo deployment/mainframes-java-app -n mainframes-app
```

**Rollback to Specific Revision:**
```bash
oc rollout undo deployment/mainframes-java-app --to-revision=2 -n mainframes-app
```

### 10.3 Image Rollback

**Redeploy Previous Image:**
```bash
# Find previous image tag
oc describe deployment mainframes-java-app -n mainframes-app | grep Image

# Update to previous tag
helm upgrade mainframes-java-app ./helm/mainframes-java-app \
  --set image.tag=previous-tag \
  --reuse-values
```

### 10.4 Automated Rollback in GitHub Actions

The CD workflow includes automatic rollback on failure. Check logs if rollback occurs.

---

## Additional Resources

### Setting up Self-hosted s390x Runner

For faster builds, set up a self-hosted runner on s390x hardware:

1. **On s390x Machine:**
```bash
# Download runner
mkdir actions-runner && cd actions-runner
curl -o actions-runner-linux-s390x-2.311.0.tar.gz -L \
  https://github.com/actions/runner/releases/download/v2.311.0/actions-runner-linux-s390x-2.311.0.tar.gz

# Extract
tar xzf ./actions-runner-linux-s390x-2.311.0.tar.gz

# Configure
./config.sh --url https://github.com/yourusername/mainframes-java \
  --token YOUR_TOKEN

# Install as service
sudo ./svc.sh install
sudo ./svc.sh start
```

2. **Update Workflow:**
```yaml
jobs:
  build:
    runs-on: self-hosted  # Changed from ubuntu-latest
```

### Performance Metrics

**Expected Build Times:**

| Build Type | Time | Notes |
|------------|------|-------|
| Maven Build | 2-5 min | Depends on dependencies |
| Docker s390x (QEMU) | 15-30 min | Emulated, slow |
| Docker s390x (Native) | 3-5 min | On s390x runner |
| Helm Deployment | 2-5 min | Network dependent |

### Useful Commands Reference

```bash
# Quick deployment check
oc get all -n mainframes-app

# Full pod describe
oc describe pod <pod-name> -n mainframes-app

# Stream logs
oc logs -f <pod-name> -n mainframes-app

# Execute command in pod
oc exec -it <pod-name> -n mainframes-app -- /bin/sh

# Port forward
oc port-forward svc/mainframes-java-app 8080:8080 -n mainframes-app

# Scale deployment
oc scale deployment mainframes-java-app --replicas=3 -n mainframes-app

# Check resource usage
oc adm top pods -n mainframes-app
oc adm top nodes -l kubernetes.io/arch=s390x
```

---

## Conclusion

This guide covered the complete end-to-end process of building and deploying a Java application to s390x OpenShift using GitHub Actions. Key takeaways:

1. **Build times** for s390x with QEMU are slower - plan accordingly
2. **Test thoroughly** on actual s390x hardware when possible
3. **Monitor closely** during first deployment
4. **Have rollback plan** ready
5. **Use caching** to optimize build times
6. **Follow security best practices** for production deployments

For additional help, refer to:
- [README.md](../README.md)
- [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [OpenShift Documentation](https://docs.openshift.com/)
- [Helm Documentation](https://helm.sh/docs/)
