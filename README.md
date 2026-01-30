# Mainframes Java Application

A production-ready Spring Boot REST API application designed for deployment on s390x OpenShift clusters using GitHub Actions and Helm.

[![CI](https://github.com/yourusername/mainframes-java/actions/workflows/ci.yml/badge.svg)](https://github.com/yourusername/mainframes-java/actions/workflows/ci.yml)
[![CD](https://github.com/yourusername/mainframes-java/actions/workflows/cd.yml/badge.svg)](https://github.com/yourusername/mainframes-java/actions/workflows/cd.yml)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Development](#development)
- [Building for s390x](#building-for-s390x)
- [Deployment](#deployment)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)

## 🎯 Overview

This application demonstrates a production-standard Java Spring Boot microservice that:
- Builds successfully on s390x architecture using GitHub Actions
- Deploys to OpenShift s390x clusters using Helm
- Includes comprehensive CI/CD pipelines
- Provides health checks, metrics, and observability
- Follows security best practices

## ✨ Features

- **RESTful API** with Spring Boot 3.2
- **Multi-architecture support** (s390x, amd64)
- **Production-ready** with health checks and metrics
- **OpenShift optimized** with proper security contexts
- **Helm charts** for easy deployment
- **GitHub Actions** CI/CD pipelines
- **Swagger/OpenAPI** documentation
- **Prometheus** metrics integration
- **Comprehensive testing** with JUnit and MockMvc

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────┐
│          GitHub Actions (CI/CD)                      │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐   │
│  │  Build &   │→ │   Docker   │→ │   Deploy   │   │
│  │   Test     │  │   Build    │  │  OpenShift │   │
│  └────────────┘  └────────────┘  └────────────┘   │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│       OpenShift s390x Cluster                        │
│  ┌──────────────────────────────────────────────┐  │
│  │  Helm Release                                 │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  │  │
│  │  │   Pod 1  │  │   Pod 2  │  │   HPA    │  │  │
│  │  │ (s390x)  │  │ (s390x)  │  │          │  │  │
│  │  └──────────┘  └──────────┘  └──────────┘  │  │
│  │                                               │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  │  │
│  │  │ Service  │  │  Route   │  │ConfigMap │  │  │
│  │  └──────────┘  └──────────┘  └──────────┘  │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

## 📦 Prerequisites

### For Local Development:
- Java 17 or higher
- Maven 3.9+
- Docker 20.10+ (with Buildx support)
- Git

### For GitHub Actions:
- GitHub repository with Actions enabled
- Container registry (Quay.io, Docker Hub, etc.)
- OpenShift cluster with s390x nodes

### For OpenShift Deployment:
- OpenShift CLI (`oc`)
- Helm 3.13+
- Access to OpenShift cluster
- Registry credentials

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/mainframes-java.git
cd mainframes-java
```

### 2. Build the Application

```bash
mvn clean package
```

### 3. Run Locally

```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

### 4. Test the Application

```bash
# Run all tests
mvn test

# Access health endpoint
curl http://localhost:8080/actuator/health

# Access API
curl http://localhost:8080/api/v1/health
```

## 🛠 Development

### Project Structure

```
mainframes-java/
├── .github/
│   └── workflows/          # GitHub Actions workflows
│       ├── ci.yml          # Continuous Integration
│       ├── cd.yml          # Continuous Deployment
│       └── build-ubi.yml   # Alternative UBI build
├── helm/
│   └── mainframes-java-app/  # Helm chart
│       ├── Chart.yaml
│       ├── values.yaml
│       └── templates/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mainframes/
│   │   │       ├── controller/   # REST controllers
│   │   │       ├── service/      # Business logic
│   │   │       ├── model/        # Data models
│   │   │       ├── config/       # Configuration
│   │   │       └── exception/    # Exception handling
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-prod.yml
│   └── test/                  # Unit and integration tests
├── Dockerfile                 # Multi-stage Docker build
├── Dockerfile.ubi            # Red Hat UBI based image
├── pom.xml                   # Maven configuration
└── README.md
```

### Available Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/actuator/health` | GET | Application health status |
| `/actuator/metrics` | GET | Application metrics |
| `/actuator/prometheus` | GET | Prometheus metrics |
| `/api/v1/health` | GET | Custom health endpoint |
| `/api/v1/messages` | GET | Get all messages |
| `/api/v1/messages/{id}` | GET | Get message by ID |
| `/api/v1/messages` | POST | Create new message |
| `/api/v1/messages/{id}` | DELETE | Delete message |
| `/swagger-ui.html` | GET | API documentation |

## 🔨 Building for s390x

### Local Build with Docker Buildx

```bash
# Set up QEMU for cross-platform builds
docker run --rm --privileged tonistiigi/binfmt:latest --install all

# Create buildx builder
docker buildx create --name s390x-builder --platform linux/s390x --use

# Build for s390x
docker buildx build --platform linux/s390x -t mainframes-java-app:s390x .

# Verify architecture
docker buildx imagetools inspect mainframes-java-app:s390x
```

### GitHub Actions Build

The application automatically builds for s390x when pushed to the main branch. See `.github/workflows/cd.yml` for details.

**Key considerations for s390x builds:**

1. **Base Images**: Use multi-arch base images that support s390x
2. **QEMU Emulation**: GitHub Actions uses QEMU for cross-compilation
3. **Build Time**: s390x builds are slower due to emulation (expect 2-5x longer)
4. **Testing**: Always test on actual s390x hardware when possible

## 🚢 Deployment

### Prerequisites Setup

1. **Create GitHub Secrets:**

Go to your repository → Settings → Secrets and variables → Actions, and add:

```
REGISTRY_NAMESPACE=your-org
REGISTRY_USERNAME=your-username
REGISTRY_PASSWORD=your-password
OPENSHIFT_SERVER=https://api.your-cluster.com:6443
OPENSHIFT_TOKEN=your-openshift-token
OPENSHIFT_NAMESPACE=mainframes-app
OPENSHIFT_ROUTE_HOST=mainframes-app.apps.your-cluster.com
```

2. **Get OpenShift Token:**

```bash
oc login --server=https://api.your-cluster.com:6443
oc whoami -t
```

### Automated Deployment (GitHub Actions)

Simply push to the main branch:

```bash
git push origin main
```

The CD workflow will:
1. Build the application
2. Create multi-arch Docker image (s390x)
3. Push to container registry
4. Deploy to OpenShift using Helm
5. Run smoke tests

### Manual Deployment with Helm

```bash
# Login to OpenShift
oc login --server=https://api.your-cluster.com:6443

# Create or switch to namespace
oc new-project mainframes-app

# Create image pull secret
oc create secret docker-registry quay-pull-secret \
  --docker-server=quay.io \
  --docker-username=your-username \
  --docker-password=your-password

# Install with Helm
helm install mainframes-java-app ./helm/mainframes-java-app \
  --set image.repository=quay.io/your-org/mainframes-java-app \
  --set image.tag=latest \
  --set imagePullSecrets[0].name=quay-pull-secret \
  --set route.host=mainframes-app.apps.your-cluster.com

# Verify deployment
oc get pods
oc get route
```

### Upgrade Deployment

```bash
helm upgrade mainframes-java-app ./helm/mainframes-java-app \
  --set image.tag=v1.1.0
```

### Rollback Deployment

```bash
# List releases
helm history mainframes-java-app

# Rollback to previous version
helm rollback mainframes-java-app

# Rollback to specific revision
helm rollback mainframes-java-app 2
```

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `JAVA_OPTS` | JVM options | `-XX:+UseContainerSupport...` |
| `SERVER_PORT` | Application port | `8080` |

### Helm Values

Key configuration options in `values.yaml`:

```yaml
# Replica count
replicaCount: 2

# Image configuration
image:
  repository: quay.io/your-org/mainframes-java-app
  tag: latest

# Resource limits
resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi

# Autoscaling
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10

# Node selector for s390x
nodeSelector:
  kubernetes.io/arch: s390x
```

## 📖 API Documentation

### Swagger UI

Access the interactive API documentation at:
```
https://your-app-url/swagger-ui.html
```

### Example API Calls

**Create a Message:**
```bash
curl -X POST https://your-app-url/api/v1/messages \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hello from s390x!",
    "author": "Admin"
  }'
```

**Get All Messages:**
```bash
curl https://your-app-url/api/v1/messages
```

## 📊 Monitoring

### Prometheus Metrics

The application exposes Prometheus metrics at `/actuator/prometheus`:

```bash
curl https://your-app-url/actuator/prometheus
```

### Health Checks

**Liveness Probe:**
```bash
curl https://your-app-url/actuator/health/liveness
```

**Readiness Probe:**
```bash
curl https://your-app-url/actuator/health/readiness
```

### Logs

**View application logs:**
```bash
# Get pod name
POD=$(oc get pods -l app.kubernetes.io/name=mainframes-java-app -o jsonpath='{.items[0].metadata.name}')

# View logs
oc logs -f $POD

# View logs from all replicas
oc logs -l app.kubernetes.io/name=mainframes-java-app -f
```

## 🔧 Troubleshooting

See [TROUBLESHOOTING.md](./docs/TROUBLESHOOTING.md) for detailed troubleshooting guide.

### Common Issues

**1. s390x Build Fails in GitHub Actions**
```
Error: Failed to build for linux/s390x
```
**Solution:** Ensure QEMU is properly set up and the base image supports s390x.

**2. Pod CrashLoopBackOff**
```bash
# Check pod logs
oc logs <pod-name>

# Check pod events
oc describe pod <pod-name>

# Common causes:
# - Insufficient memory
# - Missing environment variables
# - Image pull errors
```

**3. Image Pull Errors**
```
Error: ErrImagePull
```
**Solution:** Verify image pull secret is created and has correct credentials.

**4. Slow Build Times**
```
QEMU emulation is slow
```
**Solution:** This is expected for s390x cross-compilation. Consider:
- Using self-hosted s390x runners
- Enabling Docker buildx cache
- Building only when necessary

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 👥 Support

- 📧 Email: support@mainframes.com
- 🐛 Issues: [GitHub Issues](https://github.com/yourusername/mainframes-java/issues)
- 📖 Documentation: [Wiki](https://github.com/yourusername/mainframes-java/wiki)

## 🙏 Acknowledgments

- IBM for s390x architecture support
- Red Hat OpenShift team
- Spring Boot community
- Docker Buildx maintainers
