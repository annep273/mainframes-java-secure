# Security Policy

## Supported Versions

We actively support the following versions with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

We take security seriously. If you discover a security vulnerability, please follow these steps:

### 1. Do NOT Open a Public Issue

Please **do not** open a public GitHub issue for security vulnerabilities.

### 2. Report Via Private Channel

Send details to: **security@mainframes.com**

Include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

### 3. Response Timeline

- **Initial Response**: Within 24-48 hours
- **Status Update**: Within 5 business days
- **Fix Timeline**: 
  - Critical: 7-14 days
  - High: 30 days
  - Medium: 60 days
  - Low: 90 days

### 4. Disclosure Policy

- We follow responsible disclosure practices
- Security advisories will be published after patches are released
- Credit will be given to security researchers who report responsibly

## Security Measures

This application implements:

✅ **Dependency Scanning**: OWASP Dependency Check, Snyk
✅ **Container Scanning**: Trivy
✅ **Secret Scanning**: TruffleHog
✅ **Code Analysis**: GitHub CodeQL
✅ **Rate Limiting**: Bucket4j
✅ **Security Headers**: X-Frame-Options, CSP, HSTS, etc.
✅ **CORS Protection**: Configured for specific origins
✅ **Non-root Containers**: Runs as UID 1001
✅ **Security Context**: Dropped capabilities, read-only filesystem
✅ **TLS/HTTPS**: Enforced on all routes
✅ **RBAC**: Kubernetes service accounts with minimal permissions
✅ **Image Verification**: Multi-arch image inspection
✅ **Input Validation**: Jakarta Validation on all endpoints
✅ **Error Handling**: No sensitive data in error responses

## Security Best Practices

### For Developers

1. **Never commit secrets** - Use GitHub secrets or external secret managers
2. **Keep dependencies updated** - Run `mvn versions:display-dependency-updates` regularly
3. **Review security alerts** - Check GitHub Security tab weekly
4. **Use secure defaults** - Follow principle of least privilege
5. **Test security features** - Include security tests in your PRs

### For Operators

1. **Rotate secrets regularly** - Every 90 days minimum
2. **Monitor security logs** - Set up alerts for suspicious activity
3. **Keep platform updated** - Apply OpenShift/Kubernetes security patches
4. **Use network policies** - Restrict pod-to-pod communication
5. **Enable audit logging** - Track all API access

## Known Security Considerations

### s390x Architecture

- QEMU emulation in CI/CD may have different security characteristics than native execution
- Always test on real s390x hardware before production deployment
- Verify all dependencies support s390x architecture

### Container Security

- Base images are scanned for vulnerabilities daily
- Critical CVEs trigger automatic PR creation
- Images are signed and verified before deployment

### OpenShift Security

- Pod Security Standards enforced (restricted)
- Network policies should be configured per environment
- Service mesh integration recommended for production

## Security Tools Configuration

### Trivy

- Scans for OS packages and application dependencies
- Fails build on CRITICAL vulnerabilities
- Results uploaded to GitHub Security tab

### OWASP Dependency Check

- Checks Maven dependencies against NVD database
- Fails build on CVSS ≥ 7.0
- Suppressions file for false positives

### CodeQL

- Runs security-extended query suite
- Analyzes Java code for security issues
- Integrates with GitHub Code Scanning

### TruffleHog

- Scans for committed secrets
- Checks entire git history
- Only reports verified secrets

## Contact

- **Security Email**: security@mainframes.com
- **General Support**: support@mainframes.com
- **GitHub Issues**: For non-security bugs only

## Updates

This security policy is reviewed quarterly and updated as needed.

**Last Updated**: January 29, 2026
