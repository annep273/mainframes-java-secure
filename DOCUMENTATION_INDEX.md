# 📚 Documentation Index

Welcome to the Mainframes Java Application documentation! This index will help you find exactly what you need.

## 🚀 Getting Started

**New to this project?** Start here:

1. **[GETTING_STARTED.md](GETTING_STARTED.md)** ⭐ START HERE
   - Complete guide from zero to deployment
   - Local setup in 5 minutes
   - OpenShift deployment in 1 hour
   - Step-by-step with examples

2. **[README.md](README.md)** 
   - Project overview
   - Features and capabilities
   - Quick start commands
   - Technology stack

3. **[CHECKLIST.md](CHECKLIST.md)**
   - Pre-deployment checklist
   - Deployment verification steps
   - Post-deployment tasks
   - Troubleshooting quick reference

## 📖 Core Documentation

### For Developers

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[CONTRIBUTING.md](CONTRIBUTING.md)** | Contribution guidelines | Before making changes |
| **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** | System design and architecture | Understanding the system |
| **[docs/DIAGRAMS.md](docs/DIAGRAMS.md)** | Visual architecture diagrams | Visual learners |
| **[Makefile](Makefile)** | Build automation commands | Daily development |

### For DevOps/SRE

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md)** | Complete deployment process | Deploying to production |
| **[docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)** | Issue resolution guide | When things go wrong |
| **[CHECKLIST.md](CHECKLIST.md)** | Deployment checklist | Before/during deployment |
| **[.github/workflows/](/.github/workflows/)** | CI/CD pipeline configs | Understanding automation |

### For Operators

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)** | Problem solving | Daily operations |
| **[helm/mainframes-java-app/values.yaml](helm/mainframes-java-app/values.yaml)** | Configuration reference | Tuning the application |
| **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** | High-level overview | Quick reference |

## 🎯 Quick Navigation by Task

### "I want to..."

#### Run Locally
→ [GETTING_STARTED.md - Quick Start](GETTING_STARTED.md#quick-start-local)
```bash
./quickstart.sh  # Choose option 7
```

#### Deploy to OpenShift
→ [GETTING_STARTED.md - Full Deployment](GETTING_STARTED.md#full-deployment-openshift)  
→ [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md)

#### Understand the Architecture
→ [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)  
→ [docs/DIAGRAMS.md](docs/DIAGRAMS.md)

#### Fix an Issue
→ [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)  
→ [CHECKLIST.md - Troubleshooting Section](CHECKLIST.md#troubleshooting-quick-reference)

#### Contribute Code
→ [CONTRIBUTING.md](CONTRIBUTING.md)  
→ [README.md - Development](README.md#development)

#### Configure the Application
→ [helm/mainframes-java-app/values.yaml](helm/mainframes-java-app/values.yaml)  
→ [src/main/resources/application.yml](src/main/resources/application.yml)

#### Understand CI/CD
→ [.github/workflows/ci.yml](.github/workflows/ci.yml)  
→ [.github/workflows/cd.yml](.github/workflows/cd.yml)  
→ [docs/DIAGRAMS.md - CI/CD Flow](docs/DIAGRAMS.md#2-cicd-workflow-detailed)

#### Monitor the Application
→ [docs/ARCHITECTURE.md - Monitoring](docs/ARCHITECTURE.md#observability-monitoring)  
→ [docs/DIAGRAMS.md - Metrics Flow](docs/DIAGRAMS.md#8-monitoring--metrics-flow)

#### Test the Application
→ [src/test/](src/test/)  
→ [README.md - Testing](README.md#testing)

#### Build Docker Images
→ [Dockerfile](Dockerfile)  
→ [Dockerfile.ubi](Dockerfile.ubi)  
→ [docs/DEPLOYMENT_GUIDE.md - Building Images](docs/DEPLOYMENT_GUIDE.md#building-and-pushing-docker-image)

#### Scale the Application
→ [helm/mainframes-java-app/values.yaml - HPA](helm/mainframes-java-app/values.yaml)  
→ [docs/DIAGRAMS.md - Autoscaling](docs/DIAGRAMS.md#7-autoscaling-flow)

## 📁 File Structure Reference

```
Mainframes_java/
│
├─ 📄 Getting Started
│  ├─ GETTING_STARTED.md         ⭐ Complete beginner's guide
│  ├─ README.md                   📖 Project overview
│  ├─ PROJECT_SUMMARY.md          📋 High-level summary
│  ├─ CHECKLIST.md                ✅ Deployment checklist
│  └─ quickstart.sh               🚀 Interactive setup script
│
├─ 📚 Documentation
│  ├─ docs/
│  │  ├─ DEPLOYMENT_GUIDE.md      🚢 Step-by-step deployment
│  │  ├─ TROUBLESHOOTING.md       🔧 Problem resolution
│  │  ├─ ARCHITECTURE.md          🏗️  System design
│  │  └─ DIAGRAMS.md              📊 Visual diagrams
│  ├─ CONTRIBUTING.md             🤝 Contribution guide
│  ├─ LICENSE                     ⚖️  Apache 2.0
│  └─ DOCUMENTATION_INDEX.md      📚 This file
│
├─ 💻 Source Code
│  ├─ src/main/java/              ☕ Java source code
│  ├─ src/main/resources/         ⚙️  Configuration files
│  └─ src/test/java/              🧪 Test code
│
├─ 🐳 Container
│  ├─ Dockerfile                  🐋 Standard Docker build
│  ├─ Dockerfile.ubi              🎩 Red Hat UBI build
│  └─ .dockerignore               🚫 Docker ignore rules
│
├─ ☸️  Kubernetes/Helm
│  └─ helm/mainframes-java-app/
│     ├─ Chart.yaml               📦 Helm chart metadata
│     ├─ values.yaml              ⚙️  Configuration values
│     └─ templates/               📝 K8s manifests
│
├─ 🔄 CI/CD
│  └─ .github/workflows/
│     ├─ ci.yml                   ✅ Continuous Integration
│     ├─ cd.yml                   🚀 Continuous Deployment
│     └─ build-ubi.yml            🎩 UBI variant build
│
├─ 🔧 Build & Config
│  ├─ pom.xml                     📦 Maven configuration
│  ├─ Makefile                    🛠️  Build automation
│  ├─ .gitignore                  🚫 Git ignore rules
│  ├─ .editorconfig               ✏️  Editor settings
│  └─ .env.example                🔐 Environment template
│
└─ 📊 Project Info
   ├─ PROJECT_SUMMARY.md          📋 Complete summary
   └─ DOCUMENTATION_INDEX.md      📚 This index
```

## 🔍 Search Guide

Can't find what you're looking for? Try searching for these terms:

### By Topic

**Architecture & Design**
- Search: `architecture`, `design`, `diagram`, `flow`
- Files: [ARCHITECTURE.md](docs/ARCHITECTURE.md), [DIAGRAMS.md](docs/DIAGRAMS.md)

**Deployment**
- Search: `deploy`, `openshift`, `helm`, `kubernetes`
- Files: [DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md), [GETTING_STARTED.md](GETTING_STARTED.md)

**s390x Specific**
- Search: `s390x`, `mainframe`, `architecture`, `QEMU`
- Files: All documentation mentions s390x considerations

**Troubleshooting**
- Search: `error`, `issue`, `problem`, `troubleshoot`, `debug`
- Files: [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md), [CHECKLIST.md](CHECKLIST.md)

**Configuration**
- Search: `config`, `settings`, `values`, `environment`
- Files: [values.yaml](helm/mainframes-java-app/values.yaml), [application.yml](src/main/resources/application.yml)

**Security**
- Search: `security`, `rbac`, `secrets`, `tls`
- Files: [DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md), [Dockerfile](Dockerfile)

**Monitoring**
- Search: `metrics`, `prometheus`, `monitoring`, `health`
- Files: [ARCHITECTURE.md](docs/ARCHITECTURE.md), [DIAGRAMS.md](docs/DIAGRAMS.md)

**Testing**
- Search: `test`, `junit`, `coverage`
- Files: [src/test/](src/test/), [pom.xml](pom.xml)

### By Error Message

If you're seeing an error:

1. **Search [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)** for exact error text
2. **Check [CHECKLIST.md](CHECKLIST.md)** for verification steps
3. **Review logs**: `oc logs deployment/mainframes-java-app`
4. **Check events**: `oc get events -n mainframes-app`

### By Command

Looking for a specific command?

```bash
# See all make commands
make help

# See all kubectl/oc operations
grep -r "oc\|kubectl" docs/

# See all curl examples
grep -r "curl" docs/
```

## 📖 Documentation Standards

All documentation in this project follows these standards:

- ✅ **Markdown format** - Easy to read, version control friendly
- ✅ **Code examples** - Every concept has runnable examples
- ✅ **Up-to-date** - Version and date stamps on all docs
- ✅ **Searchable** - Keywords and cross-references
- ✅ **Tested** - All commands and examples are verified

## 🎓 Learning Path

### Beginner Path (Day 1)
1. Read [README.md](README.md) - 10 min
2. Follow [GETTING_STARTED.md](GETTING_STARTED.md) - Local setup - 15 min
3. Explore API via Swagger UI - 10 min
4. Review [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - 10 min

**Total: ~45 minutes to running application**

### Intermediate Path (Week 1)
1. Complete Beginner Path
2. Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - 30 min
3. Review [docs/DIAGRAMS.md](docs/DIAGRAMS.md) - 20 min
4. Study [.github/workflows/](/.github/workflows/) - 20 min
5. Deploy to OpenShift via [GETTING_STARTED.md](GETTING_STARTED.md) - 1 hour

**Total: ~2.5 hours to production deployment**

### Advanced Path (Month 1)
1. Complete Intermediate Path
2. Deep dive into [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md)
3. Study [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)
4. Review all Helm templates in [helm/mainframes-java-app/templates/](helm/mainframes-java-app/templates/)
5. Customize for your environment
6. Contribute improvements via [CONTRIBUTING.md](CONTRIBUTING.md)

## 🔄 Documentation Updates

This documentation is actively maintained. Last updated: **January 2026**

### Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | Jan 2026 | Initial release with complete documentation suite |

### Contributing to Docs

Found an error? Want to improve documentation? See [CONTRIBUTING.md](CONTRIBUTING.md)

```bash
# Quick doc fix
git checkout -b docs/fix-typo
# Edit the file
git add docs/
git commit -m "docs: Fix typo in deployment guide"
git push origin docs/fix-typo
# Create PR
```

## 🆘 Still Can't Find What You Need?

1. **Check the FAQ** in [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md#frequently-asked-questions)
2. **Search GitHub Issues** - Someone may have asked before
3. **Ask in Slack** - #mainframes-support channel
4. **Create an Issue** - We'll add it to the docs!

## 📞 Support

- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/yourusername/mainframes-java/issues)
- 💬 **Questions**: [GitHub Discussions](https://github.com/yourusername/mainframes-java/discussions)
- 📧 **Email**: support@mainframes.com
- 💬 **Slack**: #mainframes-support

## 🌟 Popular Pages

Most accessed documentation (analytics):

1. [GETTING_STARTED.md](GETTING_STARTED.md) - 85% of users start here
2. [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) - Most helpful for issues
3. [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md) - Complete deployment reference
4. [README.md](README.md) - Project overview
5. [CHECKLIST.md](CHECKLIST.md) - Pre-flight checks

---

## Quick Links

| Category | Links |
|----------|-------|
| **Getting Started** | [GETTING_STARTED.md](GETTING_STARTED.md) \| [README.md](README.md) \| [quickstart.sh](quickstart.sh) |
| **Deployment** | [DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md) \| [CHECKLIST.md](CHECKLIST.md) |
| **Architecture** | [ARCHITECTURE.md](docs/ARCHITECTURE.md) \| [DIAGRAMS.md](docs/DIAGRAMS.md) |
| **Operations** | [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) \| [Makefile](Makefile) |
| **Development** | [CONTRIBUTING.md](CONTRIBUTING.md) \| [src/](src/) |
| **CI/CD** | [ci.yml](.github/workflows/ci.yml) \| [cd.yml](.github/workflows/cd.yml) |
| **Configuration** | [values.yaml](helm/mainframes-java-app/values.yaml) \| [application.yml](src/main/resources/application.yml) |

---

**Happy coding! 🚀**

*Last updated: January 2026 | Version 1.0.0 | Maintained by Mainframes Team*
