.PHONY: help build test run docker-build docker-run deploy clean

help:
	@echo "Available targets:"
	@echo "  build         - Build the application using Maven"
	@echo "  test          - Run tests"
	@echo "  run           - Run the application locally"
	@echo "  docker-build  - Build Docker image"
	@echo "  docker-run    - Run Docker container locally"
	@echo "  helm-install  - Install application using Helm"
	@echo "  helm-upgrade  - Upgrade application using Helm"
	@echo "  deploy        - Deploy to OpenShift"
	@echo "  clean         - Clean build artifacts"

build:
	mvn clean package -DskipTests

test:
	mvn clean test

run:
	mvn spring-boot:run

docker-build:
	docker build -t mainframes-java-app:latest .

docker-build-s390x:
	docker buildx build --platform linux/s390x -t mainframes-java-app:s390x .

docker-run:
	docker run -p 8080:8080 mainframes-java-app:latest

helm-lint:
	helm lint helm/mainframes-java-app

helm-install:
	helm install mainframes-java-app helm/mainframes-java-app

helm-upgrade:
	helm upgrade mainframes-java-app helm/mainframes-java-app

helm-uninstall:
	helm uninstall mainframes-java-app

deploy:
	@echo "Deploying to OpenShift..."
	oc apply -f openshift/

clean:
	mvn clean
	rm -rf target/
