# Deployment Checklist

## Pre-Deployment Checklist

### 1. GitHub Repository Setup
- [ ] Create new GitHub repository
- [ ] Push code to repository
- [ ] Verify all files are committed

### 2. GitHub Secrets Configuration
Navigate to: `Settings → Secrets and variables → Actions`

Required secrets:
- [ ] `OPENSHIFT_SERVER` - OpenShift API server URL
- [ ] `OPENSHIFT_TOKEN` - Service account token with deployment permissions
- [ ] `OPENSHIFT_NAMESPACE` - Target namespace/project name
- [ ] `QUAY_USERNAME` - Container registry username
- [ ] `QUAY_PASSWORD` - Container registry password

Optional secrets:
- [ ] `SLACK_WEBHOOK_URL` - For deployment notifications
- [ ] `SONAR_TOKEN` - For code quality scans

### 3. Container Registry Setup
- [ ] Create account on Quay.io (or preferred registry)
- [ ] Create repository: `your-org/mainframes-java-app`
- [ ] Set repository to public or configure pull secrets
- [ ] Test registry credentials

### 4. OpenShift Cluster Verification
- [ ] Verify cluster has s390x nodes
  ```bash
  oc get nodes -l kubernetes.io/arch=s390x
  ```
- [ ] Create/verify namespace exists
  ```bash
  oc new-project mainframes-app
  ```
- [ ] Verify sufficient cluster resources
  ```bash
  oc describe quota -n mainframes-app
  ```
- [ ] Check node availability
  ```bash
  oc get nodes -o wide
  ```

### 5. Service Account Setup (OpenShift)
- [ ] Create service account for GitHub Actions
  ```bash
  oc create sa github-deployer -n mainframes-app
  ```
- [ ] Grant necessary permissions
  ```bash
  oc adm policy add-role-to-user admin system:serviceaccount:mainframes-app:github-deployer
  ```
- [ ] Get service account token
  ```bash
  oc serviceaccounts get-token github-deployer -n mainframes-app
  ```

### 6. Image Pull Secrets (if using private registry)
- [ ] Create pull secret
  ```bash
  oc create secret docker-registry quay-pull-secret \
    --docker-server=quay.io \
    --docker-username=your-username \
    --docker-password=your-password \
    -n mainframes-app
  ```
- [ ] Link secret to service account
  ```bash
  oc secrets link default quay-pull-secret --for=pull -n mainframes-app
  ```

### 7. Local Testing
- [ ] Build application locally
  ```bash
  mvn clean package
  ```
- [ ] Run tests successfully
  ```bash
  mvn test
  ```
- [ ] Verify test coverage > 80%
- [ ] Run application locally
  ```bash
  mvn spring-boot:run
  ```
- [ ] Test endpoints manually
  - [ ] GET http://localhost:8080/actuator/health
  - [ ] GET http://localhost:8080/api/v1/health
  - [ ] POST http://localhost:8080/api/v1/messages

### 8. Docker Build Testing (Optional but Recommended)
- [ ] Build Docker image locally
  ```bash
  docker build -t mainframes-java-app:test .
  ```
- [ ] Run container locally
  ```bash
  docker run -p 8080:8080 mainframes-java-app:test
  ```
- [ ] Test application in container
- [ ] (Optional) Test s390x build with QEMU
  ```bash
  docker buildx build --platform linux/s390x -t mainframes-java-app:s390x-test .
  ```

## Deployment Checklist

### 9. Initial Deployment
- [ ] Push code to main branch
- [ ] Monitor GitHub Actions workflows
  - [ ] CI workflow completes successfully
  - [ ] CD workflow triggers
  - [ ] Docker build completes (expect 15-30 min for s390x)
  - [ ] Image pushed to registry
  - [ ] Helm deployment succeeds
  - [ ] Smoke tests pass

### 10. Verify Deployment
- [ ] Check pods are running
  ```bash
  oc get pods -n mainframes-app
  ```
- [ ] Check pod logs
  ```bash
  oc logs -f deployment/mainframes-java-app -n mainframes-app
  ```
- [ ] Verify deployment status
  ```bash
  helm status mainframes-java-app -n mainframes-app
  ```
- [ ] Check all resources created
  ```bash
  oc get all -n mainframes-app
  ```

### 11. Network & Route Verification
- [ ] Get route URL
  ```bash
  oc get route mainframes-java-app -n mainframes-app
  ```
- [ ] Verify TLS certificate
- [ ] Test route accessibility
  ```bash
  curl -k https://YOUR_ROUTE/actuator/health
  ```

### 12. Functional Testing
- [ ] Test health endpoint
  ```bash
  curl -k https://YOUR_ROUTE/actuator/health
  ```
- [ ] Test custom health endpoint
  ```bash
  curl -k https://YOUR_ROUTE/api/v1/health
  ```
- [ ] Test POST endpoint
  ```bash
  curl -k -X POST https://YOUR_ROUTE/api/v1/messages \
    -H "Content-Type: application/json" \
    -d '{"content":"Test message","author":"Tester"}'
  ```
- [ ] Test GET endpoint
  ```bash
  curl -k https://YOUR_ROUTE/api/v1/messages
  ```
- [ ] Access Swagger UI
  ```
  https://YOUR_ROUTE/swagger-ui.html
  ```

### 13. Monitoring & Observability
- [ ] Verify Prometheus metrics endpoint
  ```bash
  curl -k https://YOUR_ROUTE/actuator/prometheus
  ```
- [ ] Check ServiceMonitor is created (if Prometheus Operator installed)
  ```bash
  oc get servicemonitor -n mainframes-app
  ```
- [ ] Verify metrics in Prometheus/Grafana
- [ ] Set up alerts for critical metrics

### 14. Performance & Scaling
- [ ] Check resource usage
  ```bash
  oc adm top pods -n mainframes-app
  ```
- [ ] Verify HPA is working
  ```bash
  oc get hpa -n mainframes-app
  ```
- [ ] Test autoscaling (optional)
  ```bash
  # Generate load
  ab -n 10000 -c 100 https://YOUR_ROUTE/api/v1/health
  ```
- [ ] Watch scaling events
  ```bash
  oc get hpa -w -n mainframes-app
  ```

### 15. High Availability Testing (Optional)
- [ ] Verify multiple replicas running
  ```bash
  oc get pods -n mainframes-app
  ```
- [ ] Test pod deletion (self-healing)
  ```bash
  oc delete pod <pod-name> -n mainframes-app
  # Verify new pod is created
  ```
- [ ] Test rolling update
  ```bash
  # Make a change and push
  # Verify zero-downtime update
  ```

### 16. Security Verification
- [ ] Verify pod security context
  ```bash
  oc get pod <pod-name> -n mainframes-app -o yaml | grep -A 10 securityContext
  ```
- [ ] Check container is running as non-root
  ```bash
  oc exec <pod-name> -n mainframes-app -- id
  ```
- [ ] Verify network policies (if applicable)
- [ ] Check RBAC permissions
  ```bash
  oc describe serviceaccount mainframes-java-app -n mainframes-app
  ```

### 17. Disaster Recovery Testing
- [ ] Document backup procedures
- [ ] Test rollback capability
  ```bash
  helm rollback mainframes-java-app -n mainframes-app
  ```
- [ ] Verify automated rollback in CI/CD
- [ ] Document recovery procedures

### 18. Documentation Updates
- [ ] Update README with actual route URLs
- [ ] Document any environment-specific configurations
- [ ] Update runbook with operational procedures
- [ ] Document known issues and workarounds

## Post-Deployment Checklist

### 19. Team Onboarding
- [ ] Share deployment documentation with team
- [ ] Provide access credentials
- [ ] Conduct knowledge transfer session
- [ ] Add team members to GitHub repository

### 20. Maintenance Setup
- [ ] Set up log aggregation
- [ ] Configure backup schedules
- [ ] Set up alerting
- [ ] Document on-call procedures
- [ ] Schedule regular health checks

### 21. Continuous Improvement
- [ ] Review deployment metrics
- [ ] Identify optimization opportunities
- [ ] Plan for future enhancements
- [ ] Schedule regular security updates

## Troubleshooting Quick Reference

### Issue: Build Takes Too Long
**Solution**: s390x builds with QEMU take 15-30 min. This is normal.
- Consider self-hosted s390x runners for faster builds
- Enable build caching

### Issue: Pods Not Scheduling
**Check**:
```bash
oc describe pod <pod-name> -n mainframes-app
```
**Common causes**:
- No s390x nodes available
- Insufficient resources
- Image pull errors

### Issue: CrashLoopBackOff
**Check logs**:
```bash
oc logs <pod-name> -n mainframes-app
```
**Common causes**:
- Out of memory (increase limits)
- Failed health checks (adjust timeouts)
- Configuration errors

### Issue: Cannot Access Route
**Check route**:
```bash
oc get route mainframes-java-app -n mainframes-app
oc describe route mainframes-java-app -n mainframes-app
```
**Common causes**:
- Service not ready
- TLS certificate issues
- Network policy blocking traffic

## Emergency Contacts

- **OpenShift Support**: [your-support-channel]
- **DevOps Team**: [team-email]
- **On-Call**: [on-call-number]

## References

- [README.md](README.md) - Main project documentation
- [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md) - Detailed deployment guide
- [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) - Comprehensive troubleshooting
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - Architecture documentation

---

**Last Updated**: January 2026
**Version**: 1.0.0
