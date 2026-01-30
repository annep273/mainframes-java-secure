# Architecture Guide - s390x Java Application

## Overview

This document describes the architecture and design decisions for the Mainframes Java Application running on s390x OpenShift clusters.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Developer/CI Pipeline                    │
│                                                               │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐      │
│  │   Source    │──▶│   GitHub    │──▶│  Container  │      │
│  │  Code       │   │  Actions    │   │  Registry   │      │
│  └─────────────┘   └─────────────┘   └─────────────┘      │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│              OpenShift s390x Cluster                         │
│                                                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                  Helm Release                          │  │
│  │                                                         │  │
│  │  ┌──────────────────────────────────────────────┐    │  │
│  │  │           Load Balancer/Router                │    │  │
│  │  └──────────────────────────────────────────────┘    │  │
│  │                      │                                 │  │
│  │  ┌───────────────────┴────────────────────┐          │  │
│  │  │                                          │          │  │
│  │  ▼                                          ▼          │  │
│  │  ┌──────────────┐              ┌──────────────┐      │  │
│  │  │   Pod 1      │              │   Pod 2      │      │  │
│  │  │  (s390x)     │              │  (s390x)     │      │  │
│  │  │              │              │              │      │  │
│  │  │ Spring Boot  │              │ Spring Boot  │      │  │
│  │  │   JRE 17     │              │   JRE 17     │      │  │
│  │  └──────────────┘              └──────────────┘      │  │
│  │                                                         │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │     Horizontal Pod Autoscaler (HPA)            │  │  │
│  │  │  Min: 2  |  Max: 10  |  Target CPU: 80%        │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  │                                                         │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │              ConfigMap                          │  │  │
│  │  │  - Application Configuration                    │  │  │
│  │  │  - Environment Variables                        │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Technology Stack

### Application Layer
- **Framework**: Spring Boot 3.2.2
- **Language**: Java 17
- **Build Tool**: Maven 3.9
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Metrics**: Micrometer + Prometheus

### Container Layer
- **Base Image**: Eclipse Temurin 17 (JRE) or Red Hat UBI 9
- **Architecture**: linux/s390x
- **Build Tool**: Docker Buildx with QEMU emulation

### Orchestration Layer
- **Platform**: Red Hat OpenShift 4.x
- **Package Manager**: Helm 3.13+
- **Architecture**: s390x nodes

### CI/CD Layer
- **CI/CD Platform**: GitHub Actions
- **Container Registry**: Quay.io (recommended)
- **Build Method**: Multi-stage Docker builds with QEMU

## Design Decisions

### 1. Multi-Architecture Support

**Decision**: Use Docker Buildx with QEMU for cross-platform builds

**Rationale**:
- Allows building s390x images from x86 GitHub Actions runners
- No need for dedicated s390x build infrastructure initially
- Provides flexibility to add native s390x builds later

**Trade-offs**:
- Slower build times (3-10x) due to emulation
- May have edge cases with low-level code
- Requires careful testing on actual s390x hardware

### 2. Base Image Selection

**Decision**: Offer both Eclipse Temurin and Red Hat UBI base images

**Rationale**:
- **Eclipse Temurin**: Widely used, good documentation, smaller size
- **Red Hat UBI**: Official Red Hat support, certified for OpenShift, better compliance

**Recommendation**:
- Use UBI for production OpenShift deployments
- Use Temurin for development/testing

### 3. Multi-Stage Build

**Decision**: Use multi-stage Dockerfile to separate build and runtime

**Rationale**:
- Reduces final image size by 60-70%
- Separates build tools from runtime
- Improves security by minimizing attack surface

### 4. Health Checks

**Decision**: Implement three types of probes

**Liveness Probe**: Checks if application is alive
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
```

**Readiness Probe**: Checks if application can serve traffic
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
```

**Startup Probe**: Handles slow-starting applications
```yaml
startupProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  failureThreshold: 30
  periodSeconds: 10
```

### 5. Resource Management

**Decision**: Set conservative resource requests and limits

**CPU**:
```yaml
requests:
  cpu: 500m
limits:
  cpu: 1000m
```

**Memory**:
```yaml
requests:
  memory: 512Mi
limits:
  memory: 1Gi
```

**Rationale**:
- Prevents resource starvation
- Enables predictable performance
- Supports autoscaling decisions

### 6. Horizontal Pod Autoscaling

**Decision**: Enable HPA with CPU and memory metrics

```yaml
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
  targetMemoryUtilizationPercentage: 80
```

**Rationale**:
- Automatically scales based on load
- Maintains minimum 2 replicas for HA
- Caps at 10 replicas to control costs

### 7. Security

**Decision**: Run as non-root user with restricted permissions

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1001
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
```

**Rationale**:
- Follows OpenShift security best practices
- Reduces attack surface
- Complies with pod security standards

### 8. Monitoring

**Decision**: Expose Prometheus metrics at `/actuator/prometheus`

**Rationale**:
- Industry standard for metrics
- Native OpenShift integration
- Rich ecosystem of tools

## API Design

### RESTful Endpoints

```
GET  /api/v1/health           - Health status
GET  /api/v1/messages         - List all messages
GET  /api/v1/messages/{id}    - Get specific message
POST /api/v1/messages         - Create message
DELETE /api/v1/messages/{id}  - Delete message
```

### Response Format

```json
{
  "id": 1,
  "content": "Message text",
  "author": "Author name",
  "createdAt": "2024-01-29T10:00:00"
}
```

### Error Handling

```json
{
  "timestamp": "2024-01-29T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Content cannot be blank",
  "path": "/api/v1/messages"
}
```

## Data Flow

### Request Flow

```
User Request
    ↓
OpenShift Route (TLS Termination)
    ↓
Kubernetes Service (Load Balancer)
    ↓
Pod (Spring Boot Application)
    ↓
Controller Layer
    ↓
Service Layer
    ↓
Response
```

### Build and Deployment Flow

```
Code Push
    ↓
GitHub Actions Trigger
    ↓
Run Tests (CI)
    ↓
Build JAR with Maven
    ↓
Build Docker Image (s390x)
    ↓
Push to Container Registry
    ↓
Deploy with Helm
    ↓
OpenShift Rolling Update
    ↓
Health Check
    ↓
Route Traffic to New Pods
```

## Scalability Considerations

### Horizontal Scaling
- Stateless design allows unlimited horizontal scaling
- HPA automatically adds/removes pods
- Load balanced via Kubernetes Service

### Vertical Scaling
- JVM configured to use container resources efficiently
- `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`

### Database Scaling (Future)
- Currently uses in-memory storage
- Can add PostgreSQL/MongoDB for persistence
- Connection pooling for database efficiency

## Performance Optimization

### JVM Tuning
```bash
-XX:+UseG1GC                    # Use G1 garbage collector
-XX:MaxGCPauseMillis=200        # Target 200ms GC pauses
-XX:+UseStringDeduplication     # Save memory on duplicate strings
-XX:MaxRAMPercentage=75.0       # Use 75% of container memory
```

### Caching Strategy
- In-memory caching with ConcurrentHashMap
- Can add Redis for distributed caching

### Connection Management
- Tomcat connection pool defaults
- Configurable via `server.tomcat.threads.*`

## High Availability

### Pod Redundancy
- Minimum 2 replicas always running
- Anti-affinity rules spread pods across nodes

### Rolling Updates
```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 1
    maxUnavailable: 0
```

### Pod Disruption Budget
```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: mainframes-java-app-pdb
spec:
  minAvailable: 1
```

## Disaster Recovery

### Backup Strategy
- Container images stored in registry
- Configuration in Git (GitOps)
- Helm releases can be restored

### Rollback Procedure
```bash
# Quick rollback
helm rollback mainframes-java-app

# OpenShift native
oc rollout undo deployment/mainframes-java-app
```

## Observability

### Logging
- Structured logging with Logback
- Logs to stdout (captured by OpenShift)
- Centralized logging with EFK/ELK stack

### Metrics
- JVM metrics (memory, GC, threads)
- HTTP request metrics
- Custom business metrics

### Tracing
- Can integrate with Jaeger/Zipkin
- Distributed tracing for microservices

### Alerting
- Prometheus AlertManager integration
- Alerts on:
  - High memory usage
  - High error rates
  - Pod restart count
  - Response time degradation

## Future Enhancements

1. **Database Integration**
   - Add PostgreSQL or MongoDB
   - Implement data persistence layer

2. **Caching Layer**
   - Add Redis for distributed caching
   - Improve response times

3. **Message Queue**
   - Add Kafka/RabbitMQ for async processing
   - Improve scalability

4. **Service Mesh**
   - Integrate with Istio
   - Advanced traffic management
   - mTLS between services

5. **Native s390x Builds**
   - Set up self-hosted s390x runners
   - Reduce build times significantly

## References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/)
- [OpenShift Documentation](https://docs.openshift.com/)
- [Helm Documentation](https://helm.sh/docs/)
- [s390x Architecture](https://www.ibm.com/docs/en/linux-on-systems)
- [Docker Multi-arch Builds](https://docs.docker.com/build/building/multi-platform/)
