#!/bin/bash

# Mainframes Java Application - Quick Start Script
# This script helps you quickly set up and deploy the application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}╔═══════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  Mainframes Java Application Quick Start         ║${NC}"
echo -e "${GREEN}║  s390x OpenShift Deployment                       ║${NC}"
echo -e "${GREEN}╚═══════════════════════════════════════════════════╝${NC}"
echo ""

# Function to print status
print_status() {
    echo -e "${YELLOW}[*]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# Check prerequisites
print_status "Checking prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    print_success "Java $JAVA_VERSION found"
else
    print_error "Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
    print_success "Maven $MVN_VERSION found"
else
    print_error "Maven not found. Please install Maven 3.9+."
    exit 1
fi

# Check Docker
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | awk '{print $3}' | tr -d ',')
    print_success "Docker $DOCKER_VERSION found"
else
    print_error "Docker not found. Please install Docker."
    exit 1
fi

echo ""
print_status "Prerequisites check complete!"
echo ""

# Ask what to do
echo "What would you like to do?"
echo "1) Build application locally"
echo "2) Run tests"
echo "3) Run application locally"
echo "4) Build Docker image (x86)"
echo "5) Build Docker image (s390x - requires QEMU)"
echo "6) Setup OpenShift deployment"
echo "7) Full local setup (build + test + run)"
echo "0) Exit"
echo ""
read -p "Enter your choice [0-7]: " choice

case $choice in
    1)
        print_status "Building application with Maven..."
        mvn clean package
        print_success "Build complete! JAR file created in target/"
        ;;
    
    2)
        print_status "Running tests..."
        mvn test
        print_success "Tests complete!"
        ;;
    
    3)
        print_status "Starting application..."
        print_status "Application will be available at http://localhost:8080"
        print_status "Health check: http://localhost:8080/actuator/health"
        print_status "Swagger UI: http://localhost:8080/swagger-ui.html"
        print_status "Press Ctrl+C to stop"
        mvn spring-boot:run
        ;;
    
    4)
        print_status "Building Docker image for x86..."
        docker build -t mainframes-java-app:latest .
        print_success "Docker image built successfully!"
        echo ""
        print_status "To run the image:"
        echo "  docker run -p 8080:8080 mainframes-java-app:latest"
        ;;
    
    5)
        print_status "Setting up QEMU for s390x builds..."
        docker run --rm --privileged tonistiigi/binfmt:latest --install all
        
        print_status "Creating buildx builder..."
        docker buildx create --name s390x-builder --platform linux/s390x --use 2>/dev/null || \
        docker buildx use s390x-builder
        
        print_status "Building Docker image for s390x..."
        print_status "This may take 15-30 minutes due to emulation..."
        docker buildx build --platform linux/s390x -t mainframes-java-app:s390x --load .
        
        print_success "Docker image built successfully for s390x!"
        
        print_status "Verifying architecture..."
        docker inspect mainframes-java-app:s390x | grep Architecture
        ;;
    
    6)
        print_status "Setting up OpenShift deployment..."
        echo ""
        
        if ! command -v oc &> /dev/null; then
            print_error "OpenShift CLI (oc) not found. Please install it first."
            exit 1
        fi
        
        if ! command -v helm &> /dev/null; then
            print_error "Helm not found. Please install Helm 3.13+."
            exit 1
        fi
        
        read -p "OpenShift server URL: " OC_SERVER
        read -p "OpenShift token: " OC_TOKEN
        read -p "Project/Namespace (default: mainframes-app): " OC_PROJECT
        OC_PROJECT=${OC_PROJECT:-mainframes-app}
        
        read -p "Container registry (default: quay.io): " REGISTRY
        REGISTRY=${REGISTRY:-quay.io}
        
        read -p "Registry namespace/org: " REGISTRY_NS
        read -p "Registry username: " REGISTRY_USER
        read -sp "Registry password: " REGISTRY_PASS
        echo ""
        
        print_status "Logging in to OpenShift..."
        oc login --token="$OC_TOKEN" --server="$OC_SERVER"
        
        print_status "Creating/switching to project $OC_PROJECT..."
        oc project "$OC_PROJECT" 2>/dev/null || oc new-project "$OC_PROJECT"
        
        print_status "Creating image pull secret..."
        oc create secret docker-registry quay-pull-secret \
            --docker-server="$REGISTRY" \
            --docker-username="$REGISTRY_USER" \
            --docker-password="$REGISTRY_PASS" \
            -n "$OC_PROJECT" --dry-run=client -o yaml | oc apply -f -
        
        print_status "Installing application with Helm..."
        helm upgrade --install mainframes-java-app ./helm/mainframes-java-app \
            --set image.repository="$REGISTRY/$REGISTRY_NS/mainframes-java-app" \
            --set image.tag=latest \
            --set imagePullSecrets[0].name=quay-pull-secret \
            --namespace "$OC_PROJECT"
        
        print_success "Deployment initiated!"
        echo ""
        print_status "Checking deployment status..."
        oc get pods -n "$OC_PROJECT"
        
        print_status "Getting route..."
        ROUTE=$(oc get route mainframes-java-app -n "$OC_PROJECT" -o jsonpath='{.spec.host}' 2>/dev/null || echo "Not available yet")
        
        if [ "$ROUTE" != "Not available yet" ]; then
            print_success "Application will be available at: https://$ROUTE"
        fi
        ;;
    
    7)
        print_status "Running full local setup..."
        
        print_status "Step 1/3: Building application..."
        mvn clean package
        print_success "Build complete!"
        
        echo ""
        print_status "Step 2/3: Running tests..."
        mvn test
        print_success "Tests passed!"
        
        echo ""
        print_status "Step 3/3: Starting application..."
        print_success "Setup complete!"
        print_status "Application starting at http://localhost:8080"
        print_status "Press Ctrl+C to stop"
        echo ""
        mvn spring-boot:run
        ;;
    
    0)
        print_status "Exiting..."
        exit 0
        ;;
    
    *)
        print_error "Invalid choice. Please run the script again."
        exit 1
        ;;
esac

echo ""
print_success "Done!"
