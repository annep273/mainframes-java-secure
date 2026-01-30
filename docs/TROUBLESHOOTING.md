# Troubleshooting Guide for s390x Deployment

This guide provides detailed troubleshooting steps for common issues encountered when building and deploying Java applications on s390x architecture.

## Table of Contents

1. [Build Issues](#build-issues)
2. [Container Issues](#container-issues)
3. [Deployment Issues](#deployment-issues)
4. [Runtime Issues](#runtime-issues)
5. [Networking Issues](#networking-issues)
6. [Performance Issues](#performance-issues)
7. [Security Issues](#security-issues)
8. [Debug Tools](#debug-tools)

---

## Build Issues

### Maven Build Failures

#### Issue: Out of Memory During Build

**Symptoms:**
```
java.lang.OutOfMemoryError: Java heap space
The build failed due to insufficient memory
```

**Solution:**
```bash
# Increase Maven memory
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
mvn clean package

# Or in pom.xml
<configuration>
  <argLine>-Xmx2048m</argLine>
</configuration>
```

#### Issue: Dependency Download Failures

**Symptoms:**
```
Could not resolve dependencies for project
Connection timeout: connect
```

**Solutions:**

1. Check network connectivity:
```bash
curl -I https://repo.maven.apache.org/maven2/
```

2. Use Maven retry:
```bash
mvn clean package -U --fail-at-end
```

3. Configure proxy if needed:
```xml
<!-- In ~/.m2/settings.xml -->
<proxies>
  <proxy>
    <host>proxy.example.com</host>
    <port>8080</port>
  </proxy>
</proxies>
```

#### Issue: Test Failures Blocking Build

**Symptoms:**
```
Tests run: 10, Failures: 2, Errors: 0, Skipped: 0
BUILD FAILURE
```

**Solutions:**

1. Run tests in isolation:
```bash
mvn test -Dtest=HealthControllerTest
```

2. Skip tests temporarily:
```bash
mvn package -DskipTests
```

3. Check test logs:
```bash
cat target/surefire-reports/*.txt
```

---

## Container Issues

### Docker Build Issues for s390x

#### Issue: Platform Not Supported

**Symptoms:**
```
ERROR: failed to solve: eclipse-temurin:17-jre-alpine: 
no match for platform in manifest: not found
```

**Cause:** Base image doesn't support s390x

**Solutions:**

1. Verify image supports s390x:
```bash
docker manifest inspect eclipse-temurin:17-jre-alpine | grep s390x
```

2. Use alternative base image:
```dockerfile
# Instead of alpine (often no s390x)
FROM eclipse-temurin:17-jre

# Or Red Hat UBI (always has s390x)
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.18
```

3. Check Docker Hub for platform support:
```bash
docker buildx imagetools inspect <image-name>
```

#### Issue: QEMU Emulation Crashes

**Symptoms:**
```
qemu: uncaught target signal 11 (Segmentation fault)
```

**Solutions:**

1. Update QEMU:
```bash
docker run --rm --privileged tonistiigi/binfmt:latest --uninstall qemu-*
docker run --rm --privileged tonistiigi/binfmt:latest --install all
```

2. Use newer buildx version:
```bash
docker buildx version
docker buildx create --driver-opt image=moby/buildkit:latest
```

3. Try different base image or build stage

#### Issue: Slow s390x Builds

**Expected:** s390x builds with QEMU are 3-10x slower than native

**Optimizations:**

1. **Enable Layer Caching:**
```yaml
cache-from: type=registry,ref=quay.io/org/app:buildcache
cache-to: type=registry,ref=quay.io/org/app:buildcache,mode=max
```

2. **Optimize Dockerfile:**
```dockerfile
# Bad: Everything in one layer
COPY . .
RUN mvn package

# Good: Separate layers for caching
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests
```

3. **Use Self-hosted s390x Runner** (fastest option)

4. **Parallel Builds:**
```bash
docker buildx build --platform linux/s390x --load \
  --build-arg MAVEN_THREADS=4 .
```

#### Issue: Image Size Too Large

**Symptoms:**
```
Image size: 1.2GB (too large for efficient deployment)
```

**Solutions:**

1. Use multi-stage build:
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
COPY --from=build /app/target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
```

2. Use JRE instead of JDK:
```dockerfile
# JDK: ~400MB
FROM eclipse-temurin:17

# JRE: ~200MB
FROM eclipse-temurin:17-jre
```

3. Clean unnecessary files:
```dockerfile
RUN mvn clean package && \
    rm -rf ~/.m2/repository
```

---

## Deployment Issues

### OpenShift Access Issues

#### Issue: Cannot Login to OpenShift

**Symptoms:**
```
error: The server was unable to return a response in the time allotted
Unable to connect to the server: dial tcp: lookup api.cluster.com: no such host
```

**Solutions:**

1. Verify cluster URL:
```bash
# Check URL format
# Should be: https://api.<cluster-name>:<port>
oc login --server=https://api.mycluster.example.com:6443
```

2. Check network connectivity:
```bash
ping api.mycluster.example.com
curl -k https://api.mycluster.example.com:6443/healthz
```

3. Verify token:
```bash
# Get fresh token
oc login --username=admin --password=password
oc whoami -t
```

4. Check firewall rules:
```bash
telnet api.mycluster.example.com 6443
```

#### Issue: Insufficient Permissions

**Symptoms:**
```
Error from server (Forbidden): pods is forbidden: 
User "system:serviceaccount:default:deployer" cannot list resource "pods"
```

**Solutions:**

1. Check current permissions:
```bash
oc auth can-i create deployments
oc auth can-i create routes
```

2. Grant necessary permissions:
```bash
# For service account
oc adm policy add-role-to-user admin -z github-deployer -n mainframes-app

# For user
oc adm policy add-cluster-role-to-user cluster-admin username
```

3. Verify role bindings:
```bash
oc get rolebindings -n mainframes-app
```

### Pod Scheduling Issues

#### Issue: No s390x Nodes Available

**Symptoms:**
```
0/10 nodes are available: 10 node(s) didn't match Pod's node affinity/selector
```

**Diagnosis:**
```bash
# Check for s390x nodes
oc get nodes -l kubernetes.io/arch=s390x

# If no nodes found
oc get nodes -o wide

# Check node labels
oc get nodes --show-labels | grep arch
```

**Solutions:**

1. If no s390x nodes exist, you need to:
   - Add s390x worker nodes to cluster
   - Or remove s390x node selector temporarily

2. Temporarily remove node selector:
```bash
helm upgrade mainframes-java-app ./helm/mainframes-java-app \
  --set nodeSelector=null
```

3. Label existing nodes if architecture not labeled:
```bash
oc label node <node-name> kubernetes.io/arch=s390x
```

#### Issue: Insufficient Resources

**Symptoms:**
```
0/3 nodes are available: 3 Insufficient memory, 3 Insufficient cpu
```

**Solutions:**

1. Check node resources:
```bash
oc describe nodes | grep -A 5 "Allocated resources"
```

2. Reduce resource requests:
```yaml
resources:
  requests:
    cpu: 250m      # Reduced from 500m
    memory: 256Mi  # Reduced from 512Mi
```

3. Add more nodes or increase node capacity

#### Issue: Pod Stuck in Pending

**Symptoms:**
```
NAME                                READY   STATUS    RESTARTS   AGE
mainframes-java-app-xxxxx-xxxxx    0/1     Pending   0          5m
```

**Diagnosis:**
```bash
# Check pod events
oc describe pod <pod-name>

# Look for:
# - "Insufficient cpu/memory"
# - "FailedScheduling"
# - "PodSandboxNotReady"
```

**Solutions:**

1. Check PVC if using persistent storage:
```bash
oc get pvc
# Verify PVC is Bound
```

2. Check for pod priority:
```bash
oc get priorityclasses
```

3. Temporarily increase timeouts:
```bash
# Wait longer
kubectl wait --for=condition=Ready pod/<pod-name> --timeout=600s
```

### Image Pull Issues

#### Issue: ErrImagePull / ImagePullBackOff

**Symptoms:**
```
Failed to pull image "quay.io/org/app:latest": 
rpc error: code = Unknown desc = Error reading manifest latest
```

**Solutions:**

1. Verify image exists:
```bash
docker pull quay.io/your-org/mainframes-java-app:latest
```

2. Check image architecture:
```bash
docker inspect quay.io/your-org/mainframes-java-app:latest | grep Architecture
# Should show: s390x
```

3. Recreate image pull secret:
```bash
# Delete old secret
oc delete secret quay-pull-secret -n mainframes-app

# Create new secret
oc create secret docker-registry quay-pull-secret \
  --docker-server=quay.io \
  --docker-username=$USERNAME \
  --docker-password=$PASSWORD \
  -n mainframes-app

# Link to service account
oc secrets link default quay-pull-secret --for=pull -n mainframes-app
```

4. Make image public temporarily for testing:
```bash
# In Quay.io web interface
Repository Settings → Make Public
```

5. Check secret is referenced in deployment:
```bash
oc get deployment mainframes-java-app -o yaml | grep -A 2 imagePullSecrets
```

---

## Runtime Issues

### Application Crashes

#### Issue: CrashLoopBackOff

**Symptoms:**
```
NAME                               READY   STATUS             RESTARTS   AGE
mainframes-java-app-xxxxx-xxxxx   0/1     CrashLoopBackOff   5          3m
```

**Diagnosis:**
```bash
# Check current logs
oc logs <pod-name>

# Check previous container logs
oc logs <pod-name> --previous

# Watch logs in real-time
oc logs <pod-name> -f

# Check events
oc get events --sort-by='.lastTimestamp' -n mainframes-app
```

**Common Causes:**

1. **Java OOM (Out of Memory):**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
```yaml
# Increase memory
resources:
  limits:
    memory: 2Gi
  requests:
    memory: 1Gi

# Adjust JVM heap
env:
  - name: JAVA_OPTS
    value: "-Xms512m -Xmx1536m"
```

2. **Missing Environment Variables:**
```
Error: Required configuration property 'DATABASE_URL' not found
```

**Solution:**
```bash
helm upgrade mainframes-java-app ./helm/mainframes-java-app \
  --set env[0].name=DATABASE_URL \
  --set env[0].value=jdbc:postgresql://db:5432/mydb
```

3. **Port Binding Issues:**
```
Port 8080 is already in use
```

**Solution:**
```yaml
# Change port
server:
  port: 8081
```

#### Issue: Application Not Starting

**Symptoms:**
```
Container started but application not responding
Health checks failing
```

**Diagnosis:**
```bash
# Check startup logs
oc logs <pod-name> | grep -i "started\|error\|exception"

# Port-forward and test locally
oc port-forward <pod-name> 8080:8080
curl http://localhost:8080/actuator/health
```

**Solutions:**

1. Increase startup timeouts:
```yaml
livenessProbe:
  initialDelaySeconds: 120  # Increased
startupProbe:
  failureThreshold: 30      # More retries
  periodSeconds: 10
```

2. Check Java startup time:
```bash
# Add verbose GC logging
env:
  - name: JAVA_OPTS
    value: "-verbose:gc -XX:+PrintGCDetails"
```

### Memory Leaks

#### Issue: Pod Memory Keeps Increasing

**Symptoms:**
```
Pod memory usage: 80% → 90% → 95% → OOMKilled
```

**Diagnosis:**

1. Monitor memory:
```bash
oc adm top pod <pod-name>
```

2. Get heap dump:
```bash
# Port-forward JMX port
oc port-forward <pod-name> 9010:9010

# Or exec into pod
oc exec -it <pod-name> -- jcmd 1 GC.heap_dump /tmp/heap.hprof
```

**Solutions:**

1. Limit heap size:
```bash
env:
  - name: JAVA_OPTS
    value: "-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError"
```

2. Enable container awareness:
```bash
env:
  - name: JAVA_OPTS
    value: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

3. Use G1GC for better memory management:
```bash
env:
  - name: JAVA_OPTS
    value: "-XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

---

## Networking Issues

### Route/Service Not Accessible

#### Issue: Cannot Access Application URL

**Symptoms:**
```
curl: (7) Failed to connect to app.cluster.com port 443: Connection refused
```

**Diagnosis:**

1. Check route exists:
```bash
oc get route -n mainframes-app
```

2. Check service endpoints:
```bash
oc get svc mainframes-java-app -n mainframes-app
oc get endpoints mainframes-java-app -n mainframes-app
# Should show pod IPs
```

3. Check pod status:
```bash
oc get pods -n mainframes-app
# All pods should be Running and Ready
```

**Solutions:**

1. If route doesn't exist:
```bash
oc expose service mainframes-java-app -n mainframes-app
```

2. If service has no endpoints:
```bash
# Check label selectors match
oc get svc mainframes-java-app -o yaml | grep -A 3 selector
oc get pods --show-labels -n mainframes-app
```

3. Test from inside cluster:
```bash
oc run test --image=curlimages/curl --rm -it -- \
  curl http://mainframes-java-app:8080/actuator/health
```

#### Issue: DNS Resolution Failures

**Symptoms:**
```
curl: (6) Could not resolve host: mainframes-java-app.apps.cluster.com
```

**Solutions:**

1. Check route host:
```bash
oc get route mainframes-java-app -o yaml | grep host
```

2. Test DNS:
```bash
nslookup mainframes-java-app.apps.cluster.com
dig mainframes-java-app.apps.cluster.com
```

3. Check cluster DNS:
```bash
oc get svc -n openshift-dns
```

### TLS/SSL Issues

#### Issue: SSL Certificate Errors

**Symptoms:**
```
curl: (60) SSL certificate problem: self signed certificate
```

**Solutions:**

1. Use insecure flag for testing:
```bash
curl -k https://app.cluster.com/health
```

2. Check certificate:
```bash
openssl s_client -connect app.cluster.com:443 -servername app.cluster.com
```

3. Configure proper certificates:
```yaml
route:
  tls:
    enabled: true
    termination: edge
    certificate: |
      -----BEGIN CERTIFICATE-----
      ...
```

---

## Performance Issues

### Slow Response Times

#### Issue: High Latency

**Diagnosis:**

1. Check pod resources:
```bash
oc adm top pods -n mainframes-app
```

2. Enable metrics:
```bash
curl https://app.cluster.com/actuator/metrics
```

3. Check JVM metrics:
```bash
curl https://app.cluster.com/actuator/metrics/jvm.memory.used
curl https://app.cluster.com/actuator/metrics/jvm.gc.pause
```

**Solutions:**

1. Increase resources:
```yaml
resources:
  limits:
    cpu: 2000m
    memory: 2Gi
```

2. Enable HPA:
```yaml
autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
```

3. Optimize JVM:
```bash
env:
  - name: JAVA_OPTS
    value: >-
      -XX:+UseG1GC
      -XX:MaxGCPauseMillis=200
      -XX:ParallelGCThreads=4
      -XX:ConcGCThreads=2
      -XX:InitiatingHeapOccupancyPercent=70
```

### High CPU Usage

**Diagnosis:**
```bash
oc adm top pod <pod-name>
# CPU usage consistently > 80%
```

**Solutions:**

1. Profile application:
```bash
# Enable JFR (Java Flight Recorder)
env:
  - name: JAVA_OPTS
    value: >-
      -XX:+UnlockDiagnosticVMOptions
      -XX:+DebugNonSafepoints
      -XX:FlightRecorderOptions=stackdepth=256
```

2. Optimize code (check profiling results)

3. Scale horizontally:
```bash
oc scale deployment mainframes-java-app --replicas=5
```

---

## Security Issues

### Pod Security Violations

#### Issue: SecurityContextConstraint Errors

**Symptoms:**
```
unable to validate against any security context constraint
pods "mainframes-java-app-xxxxx" is forbidden: unable to validate against any pod security policy
```

**Solutions:**

1. Check SCC:
```bash
oc get scc
oc describe scc restricted
```

2. Grant SCC to service account:
```bash
oc adm policy add-scc-to-user anyuid -z mainframes-sa
```

3. Use less restrictive security context:
```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1001
  fsGroup: 1001
```

---

## Debug Tools

### Essential Commands

```bash
# Pod debugging
oc debug pod/<pod-name>
oc rsh <pod-name>
oc exec -it <pod-name> -- /bin/bash

# Resource inspection
oc describe pod <pod-name>
oc get pod <pod-name> -o yaml
oc logs <pod-name> --tail=100

# Events
oc get events --sort-by='.lastTimestamp'
oc get events --field-selector involvedObject.name=<pod-name>

# Resources
oc adm top nodes
oc adm top pods

# Network debugging
oc run test --image=nicolaka/netshoot --rm -it -- /bin/bash
```

### Enable Debug Logging

```yaml
# In values.yaml or application.yml
logging:
  level:
    root: DEBUG
    com.mainframes: TRACE
    org.springframework: DEBUG
```

### Collect Diagnostic Bundle

```bash
#!/bin/bash
# Save to debug.sh

NAMESPACE="mainframes-app"
OUTPUT_DIR="diagnostics-$(date +%Y%m%d-%H%M%S)"

mkdir -p "$OUTPUT_DIR"

echo "Collecting diagnostics..."

# Pods
oc get pods -n "$NAMESPACE" -o yaml > "$OUTPUT_DIR/pods.yaml"

# Logs
for pod in $(oc get pods -n "$NAMESPACE" -o name); do
    pod_name=$(basename "$pod")
    oc logs "$pod" -n "$NAMESPACE" > "$OUTPUT_DIR/${pod_name}.log" 2>&1
    oc logs "$pod" -n "$NAMESPACE" --previous > "$OUTPUT_DIR/${pod_name}-previous.log" 2>&1
done

# Deployment
oc get deployment -n "$NAMESPACE" -o yaml > "$OUTPUT_DIR/deployment.yaml"

# Service
oc get svc -n "$NAMESPACE" -o yaml > "$OUTPUT_DIR/service.yaml"

# Route
oc get route -n "$NAMESPACE" -o yaml > "$OUTPUT_DIR/route.yaml"

# Events
oc get events -n "$NAMESPACE" --sort-by='.lastTimestamp' > "$OUTPUT_DIR/events.txt"

# Helm
helm list -n "$NAMESPACE" > "$OUTPUT_DIR/helm-releases.txt"
helm history mainframes-java-app -n "$NAMESPACE" > "$OUTPUT_DIR/helm-history.txt"

echo "Diagnostics collected in $OUTPUT_DIR"
tar czf "$OUTPUT_DIR.tar.gz" "$OUTPUT_DIR"
echo "Archive created: $OUTPUT_DIR.tar.gz"
```

---

## Getting Help

If you're still stuck after trying these troubleshooting steps:

1. **Check logs thoroughly** - 90% of issues are revealed in logs
2. **Search GitHub Issues** - Someone may have encountered the same problem
3. **Check OpenShift/Kubernetes documentation**
4. **Ask in community forums**:
   - OpenShift Community
   - Stack Overflow (tag: openshift, kubernetes, s390x)
   - Spring Boot Community

5. **Create a detailed bug report** including:
   - Full error message
   - Steps to reproduce
   - Environment details (OpenShift version, Java version, etc.)
   - Relevant logs
   - What you've already tried

---

**Last Updated:** $(date)
**Version:** 1.0.0
