# Visual Architecture & Flow Diagrams

## 1. Overall Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         DEVELOPER WORKFLOW                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Developer ─────> GitHub Repository ─────> GitHub Actions          │
│                         │                          │                │
│                         │                          │                │
│                         └──────────────────────────┘                │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         CI/CD PIPELINE                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐           │
│  │   CI Stage   │───│  Build Stage │───│ Deploy Stage │           │
│  │              │   │              │   │              │           │
│  │ • Unit Tests │   │ • Maven      │   │ • Helm       │           │
│  │ • Integration│   │ • Docker     │   │ • OpenShift  │           │
│  │ • Coverage   │   │ • s390x      │   │ • Verify     │           │
│  └──────────────┘   └──────────────┘   └──────────────┘           │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     CONTAINER REGISTRY                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│             Quay.io / Docker Hub / Private Registry                 │
│                                                                      │
│          ┌───────────────────────────────────────┐                 │
│          │  mainframes-java-app:latest (s390x)  │                 │
│          └───────────────────────────────────────┘                 │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    OPENSHIFT CLUSTER (s390x)                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌───────────────────────────────────────────────────────────┐    │
│  │                    mainframes-app namespace                │    │
│  │                                                             │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │    │
│  │  │   Pod 1      │  │   Pod 2      │  │   Pod N      │   │    │
│  │  │  (s390x)     │  │  (s390x)     │  │  (s390x)     │   │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘   │    │
│  │          ▲                 ▲                 ▲            │    │
│  │          └─────────────────┴─────────────────┘            │    │
│  │                          │                                 │    │
│  │                          ▼                                 │    │
│  │                  ┌──────────────┐                         │    │
│  │                  │   Service    │                         │    │
│  │                  └──────────────┘                         │    │
│  │                          │                                 │    │
│  │                          ▼                                 │    │
│  │                  ┌──────────────┐                         │    │
│  │                  │     Route    │                         │    │
│  │                  │  (TLS/HTTPS) │                         │    │
│  │                  └──────────────┘                         │    │
│  └───────────────────────────────────────────────────────────┘    │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         END USERS                                    │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│     Browsers, Mobile Apps, API Clients, Services                   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

## 2. CI/CD Workflow Detailed

```
┌──────────────────────────────────────────────────────────────────────┐
│                      GITHUB ACTIONS CI/CD                            │
└──────────────────────────────────────────────────────────────────────┘

  TRIGGER: Push to main / Pull Request
           │
           ▼
  ┌─────────────────────┐
  │   CI Workflow       │
  │   (ci.yml)          │
  └─────────────────────┘
           │
           ├─► Checkout Code
           │
           ├─► Setup Java 17
           │
           ├─► Cache Maven Dependencies
           │
           ├─► Run Unit Tests
           │      └─► mvn clean test
           │
           ├─► Generate Coverage Report
           │      └─► JaCoCo Analysis
           │
           ├─► Upload Test Results
           │      └─► Artifacts
           │
           └─► Build JAR
                  └─► mvn package
                         │
                         ▼
                  [CI SUCCESS]
                         │
                         ▼
  ┌─────────────────────────────────────────────────────────────┐
  │   CD Workflow (cd.yml) - Only on main branch push           │
  └─────────────────────────────────────────────────────────────┘
           │
           ├─► Setup QEMU (s390x emulation)
           │      └─► tonistiigi/binfmt:latest
           │
           ├─► Setup Docker Buildx
           │      └─► Create multi-arch builder
           │
           ├─► Login to Container Registry
           │      └─► Quay.io with credentials
           │
           ├─► Build & Push s390x Image (15-30 min)
           │      └─► docker buildx build --platform linux/s390x
           │
           ├─► Setup OpenShift CLI
           │      └─► oc login
           │
           ├─► Deploy with Helm
           │      └─► helm upgrade --install
           │
           ├─► Wait for Rollout
           │      └─► oc rollout status
           │
           ├─► Run Smoke Tests
           │      └─► curl health endpoints
           │
           └─► Success / Failure
                  │            │
                  ▼            ▼
           [DEPLOYED]    [ROLLBACK]
```

## 3. Application Request Flow

```
┌────────────────────────────────────────────────────────────────┐
│                     REQUEST LIFECYCLE                          │
└────────────────────────────────────────────────────────────────┘

External User
      │
      │ HTTPS Request
      ▼
┌─────────────────────┐
│  OpenShift Router   │  ◄─── TLS Termination
│  (HAProxy)          │       Load Balancing
└─────────────────────┘
      │
      │ HTTP
      ▼
┌─────────────────────┐
│  Service            │  ◄─── ClusterIP
│  (mainframes-app)   │       Port 8080
└─────────────────────┘
      │
      │ Round Robin
      ▼
┌─────────────────────────────────────────────────────────────┐
│              Pod Instances (s390x)                          │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  Spring Boot Application Container                   │ │
│  │                                                       │ │
│  │  ┌────────────────────────────────────────────────┐ │ │
│  │  │  Request Layer                                  │ │ │
│  │  │  • GlobalExceptionHandler                       │ │ │
│  │  │  • Request Validation                           │ │ │
│  │  └────────────────────────────────────────────────┘ │ │
│  │              │                                       │ │
│  │              ▼                                       │ │
│  │  ┌────────────────────────────────────────────────┐ │ │
│  │  │  Controller Layer                               │ │ │
│  │  │  • HealthController                             │ │ │
│  │  │  • MessageController                            │ │ │
│  │  └────────────────────────────────────────────────┘ │ │
│  │              │                                       │ │
│  │              ▼                                       │ │
│  │  ┌────────────────────────────────────────────────┐ │ │
│  │  │  Service Layer                                  │ │ │
│  │  │  • HealthService                                │ │ │
│  │  │  • MessageService                               │ │ │
│  │  └────────────────────────────────────────────────┘ │ │
│  │              │                                       │ │
│  │              ▼                                       │ │
│  │  ┌────────────────────────────────────────────────┐ │ │
│  │  │  Data Layer                                     │ │ │
│  │  │  • In-Memory Storage (ConcurrentHashMap)        │ │ │
│  │  │  • Future: Database Integration                 │ │ │
│  │  └────────────────────────────────────────────────┘ │ │
│  │                                                       │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
└─────────────────────────────────────────────────────────────┘
      │
      │ Response
      ▼
Back to User
```

## 4. Docker Multi-Stage Build Flow

```
┌────────────────────────────────────────────────────────────────┐
│                   DOCKER BUILD STAGES                          │
└────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ Stage 1: Builder (maven:3.9-eclipse-temurin-17)            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. Copy pom.xml                                           │
│  2. Download dependencies (cached layer)                   │
│  3. Copy source code                                       │
│  4. Run tests                                              │
│  5. Build JAR (mvn clean package)                         │
│                                                             │
│  Output: /workspace/target/mainframes-java-app-1.0.0.jar  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                        │
                        │ Copy JAR only
                        ▼
┌─────────────────────────────────────────────────────────────┐
│ Stage 2: Runtime (eclipse-temurin:17-jre)                  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. Create non-root user (appuser)                         │
│  2. Copy JAR from builder stage                            │
│  3. Set ownership to appuser                               │
│  4. Configure health check                                 │
│  5. Set JVM options                                        │
│  6. Expose port 8080                                       │
│                                                             │
│  Final Image: ~250MB (vs ~800MB with full builder)        │
│  Architecture: linux/s390x                                 │
│  User: appuser (non-root)                                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 5. Kubernetes/OpenShift Resource Topology

```
┌──────────────────────────────────────────────────────────────┐
│              mainframes-app Namespace                        │
└──────────────────────────────────────────────────────────────┘

namespace/mainframes-app
    │
    ├─► serviceaccount/mainframes-java-app
    │       └─► Linked to imagePullSecrets
    │
    ├─► configmap/mainframes-java-app-config
    │       └─► Application configuration
    │
    ├─► secret/quay-pull-secret (manual)
    │       └─► Registry credentials
    │
    ├─► deployment/mainframes-java-app
    │       ├─► replicas: 2 (minimum)
    │       ├─► strategy: RollingUpdate
    │       │       ├─► maxSurge: 1
    │       │       └─► maxUnavailable: 0
    │       ├─► nodeSelector: kubernetes.io/arch=s390x
    │       ├─► containers:
    │       │       ├─► image: quay.io/org/mainframes-java-app:latest
    │       │       ├─► ports: 8080
    │       │       ├─► livenessProbe: /actuator/health/liveness
    │       │       ├─► readinessProbe: /actuator/health/readiness
    │       │       ├─► startupProbe: /actuator/health
    │       │       ├─► resources:
    │       │       │       ├─► requests: 200Mi RAM, 100m CPU
    │       │       │       └─► limits: 512Mi RAM, 500m CPU
    │       │       └─► securityContext: runAsNonRoot
    │       └─► affinity: pod anti-affinity (spread across nodes)
    │
    ├─► service/mainframes-java-app
    │       ├─► type: ClusterIP
    │       ├─► port: 8080
    │       └─► selector: app=mainframes-java-app
    │
    ├─► route/mainframes-java-app
    │       ├─► host: mainframes-java-app-mainframes-app.apps...
    │       ├─► tls: edge termination
    │       └─► target: service/mainframes-java-app:8080
    │
    ├─► horizontalpodautoscaler/mainframes-java-app
    │       ├─► minReplicas: 2
    │       ├─► maxReplicas: 10
    │       ├─► targetCPU: 80%
    │       └─► targetMemory: 80%
    │
    └─► servicemonitor/mainframes-java-app (if Prometheus Operator)
            ├─► interval: 30s
            └─► path: /actuator/prometheus
```

## 6. Health Check Flow

```
┌──────────────────────────────────────────────────────────────┐
│                   HEALTH CHECK PROBES                        │
└──────────────────────────────────────────────────────────────┘

Kubernetes Probe System
    │
    ├─► Startup Probe
    │   │   • Path: /actuator/health
    │   │   • Delay: 0s
    │   │   • Period: 10s
    │   │   • Timeout: 5s
    │   │   • Failure Threshold: 30
    │   │   • Purpose: Initial container startup
    │   └─► [PASS] ───► Enable Liveness & Readiness
    │       [FAIL] ───► Restart Container
    │
    ├─► Liveness Probe
    │   │   • Path: /actuator/health/liveness
    │   │   • Delay: 60s
    │   │   • Period: 10s
    │   │   • Timeout: 5s
    │   │   • Failure Threshold: 3
    │   │   • Purpose: Detect deadlock/hung app
    │   └─► [PASS] ───► Container Healthy
    │       [FAIL] ───► Restart Container
    │
    └─► Readiness Probe
        │   • Path: /actuator/health/readiness
        │   • Delay: 30s
        │   • Period: 5s
        │   • Timeout: 5s
        │   • Failure Threshold: 3
        │   • Purpose: Ready to serve traffic
        └─► [PASS] ───► Add to Service Endpoints
            [FAIL] ───► Remove from Service Endpoints
```

## 7. Autoscaling Flow

```
┌──────────────────────────────────────────────────────────────┐
│          HORIZONTAL POD AUTOSCALER (HPA)                     │
└──────────────────────────────────────────────────────────────┘

Metrics Server
    │ (collects every 15s)
    ▼
┌────────────────────┐
│  Current Metrics   │
│  • CPU: 85%        │
│  • Memory: 70%     │
└────────────────────┘
    │
    ▼
┌────────────────────────────────────────────┐
│  HPA Controller (evaluates every 30s)      │
│                                            │
│  Target: CPU 80%, Memory 80%              │
│  Current: CPU 85%, Memory 70%             │
│                                            │
│  Decision: SCALE UP                        │
│  Current Replicas: 2                       │
│  Desired Replicas: 3                       │
└────────────────────────────────────────────┘
    │
    ▼
┌────────────────────┐
│  Update Deployment │
│  replicas: 3       │
└────────────────────┘
    │
    ▼
┌─────────────────────────────────────┐
│  Kubernetes Scheduler               │
│  • Find s390x node with capacity    │
│  • Respect anti-affinity rules      │
│  • Schedule new pod                 │
└─────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────┐
│  New Pod Starting                   │
│  1. Pull image (if not cached)      │
│  2. Start container                 │
│  3. Startup probe (30 attempts)     │
│  4. Readiness probe passes          │
│  5. Added to Service endpoints      │
└─────────────────────────────────────┘
    │
    ▼
[SCALED] - Now serving traffic with 3 replicas
```

## 8. Monitoring & Metrics Flow

```
┌──────────────────────────────────────────────────────────────┐
│                    OBSERVABILITY STACK                       │
└──────────────────────────────────────────────────────────────┘

Application (Pod)
    │
    ├─► Spring Boot Actuator
    │       │
    │       ├─► /actuator/health
    │       │       └─► Liveness & Readiness status
    │       │
    │       ├─► /actuator/metrics
    │       │       ├─► JVM metrics (heap, GC, threads)
    │       │       ├─► HTTP metrics (requests, latency)
    │       │       └─► Custom business metrics
    │       │
    │       └─► /actuator/prometheus
    │               └─► Prometheus format metrics
    │
    ├─► Container Logs (stdout/stderr)
    │       │
    │       ▼
    │   OpenShift Logging
    │       ├─► Elasticsearch
    │       └─► Kibana
    │
    └─► Prometheus ServiceMonitor
            │   (scrapes every 30s)
            ▼
        Prometheus Server
            │
            ├─► Store metrics
            ├─► Evaluate alerts
            │       └─► AlertManager
            │               └─► Slack/Email/PagerDuty
            │
            └─► Visualize in Grafana
                    └─► Dashboards
                        ├─► JVM Dashboard
                        ├─► HTTP Dashboard
                        └─► Custom Business Dashboard
```

## 9. Deployment Rollout Strategy

```
┌──────────────────────────────────────────────────────────────┐
│              ROLLING UPDATE DEPLOYMENT                       │
└──────────────────────────────────────────────────────────────┘

Initial State: v1.0 (2 replicas)
┌─────────┐  ┌─────────┐
│ Pod v1  │  │ Pod v1  │
└─────────┘  └─────────┘

Deployment Update: v1.1
    │
    ├─► Create 1 new pod (maxSurge: 1)
    │   ┌─────────┐  ┌─────────┐  ┌─────────┐
    │   │ Pod v1  │  │ Pod v1  │  │ Pod v1.1│ ◄─ Creating
    │   └─────────┘  └─────────┘  └─────────┘
    │
    ├─► Wait for readiness probe
    │   ┌─────────┐  ┌─────────┐  ┌─────────┐
    │   │ Pod v1  │  │ Pod v1  │  │ Pod v1.1│ ◄─ Ready
    │   └─────────┘  └─────────┘  └─────────┘
    │
    ├─► Terminate 1 old pod (maxUnavailable: 0)
    │   ┌─────────┐  ┌─────────┐  ┌─────────┐
    │   │ Pod v1  │  │ Pod v1  │  │ Pod v1.1│
    │   │Terminating                │
    │   └─────────┘  └─────────┘  └─────────┘
    │
    ├─► Create 2nd new pod
    │   ┌─────────┐  ┌─────────┐  ┌─────────┐
    │   │ Pod v1  │  │ Pod v1.1│  │ Pod v1.1│ ◄─ Creating
    │   └─────────┘  └─────────┘  └─────────┘
    │
    ├─► Wait for readiness probe
    │   ┌─────────┐  ┌─────────┐  ┌─────────┐
    │   │ Pod v1  │  │ Pod v1.1│  │ Pod v1.1│ ◄─ Ready
    │   └─────────┘  └─────────┘  └─────────┘
    │
    └─► Terminate last old pod
        ┌─────────┐  ┌─────────┐  ┌─────────┐
        │ Pod v1  │  │ Pod v1.1│  │ Pod v1.1│
        │Terminating                │
        └─────────┘  └─────────┘  └─────────┘

Final State: v1.1 (2 replicas)
             ┌─────────┐  ┌─────────┐
             │ Pod v1.1│  │ Pod v1.1│
             └─────────┘  └─────────┘

✅ Zero Downtime: Always 2 replicas serving traffic
✅ Gradual Rollout: One pod at a time
✅ Easy Rollback: helm rollback if issues detected
```

## 10. Build Time Comparison

```
┌──────────────────────────────────────────────────────────────┐
│              BUILD TIME COMPARISON                           │
└──────────────────────────────────────────────────────────────┘

Native x86_64 Build (GitHub-hosted runner)
├─► Maven Build: 2-3 minutes
├─► Docker Build: 1-2 minutes
└─► Total: ~3-5 minutes
    ████░░░░░░░░░░░░░░░░░░░░░░░ 20%

s390x Build with QEMU (GitHub-hosted runner)
├─► Maven Build: 8-12 minutes (4x slower)
├─► Docker Build: 7-18 minutes (10x slower)
└─► Total: ~15-30 minutes
    ████████████████████████████ 100%

Self-hosted s390x Build (native runner)
├─► Maven Build: 2-3 minutes
├─► Docker Build: 1-2 minutes
└─► Total: ~3-5 minutes
    ████░░░░░░░░░░░░░░░░░░░░░░░ 20%

💡 Recommendation: Use self-hosted s390x runners for production
```

---

**Document Version**: 1.0.0  
**Last Updated**: January 2026  
**Created By**: Mainframes Team
