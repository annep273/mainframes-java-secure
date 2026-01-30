# Multi-stage Dockerfile for s390x architecture
# Stage 1: Build stage
FROM --platform=linux/s390x maven:3.9-eclipse-temurin-17 AS build

LABEL maintainer="Mainframes Team <support@mainframes.com>"
LABEL description="Build stage for Mainframes Java Application"

WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

# Stage 2: Runtime stage
FROM --platform=linux/s390x eclipse-temurin:17-jre-jammy

LABEL maintainer="Mainframes Team <support@mainframes.com>"
LABEL description="Production runtime for Mainframes Java Application on s390x"

# Install security updates and curl for health checks
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y --no-install-recommends curl ca-certificates && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# Copy built artifacts from build stage
COPY --from=build /app/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /app/target/dependency/META-INF /app/META-INF
COPY --from=build /app/target/dependency/BOOT-INF/classes /app

# Create logs directory
RUN mkdir -p /logs && chown -R appuser:appgroup /app /logs

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Set JVM options for production
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp /app:/app/lib/* com.mainframes.MainframesApplication"]
