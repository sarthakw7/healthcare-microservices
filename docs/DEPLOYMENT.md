# Deployment Guide

## LocalStack Deployment (Recommended)

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+
- AWS CLI

### Quick Start

```bash
# 1. Start LocalStack
localstack start -d

# 2. Build images
docker build -t auth-service:latest auth-service/
docker build -t patient-service:latest patient-service/
docker build -t billing-service:latest billing-service/
docker build -t analytics-service:latest analytics-service/
docker build -t api-gateway-service:latest api-gateway/

# 3. Deploy infrastructure
cd infrastructure
mvn compile exec:java
./localstack-deploy.sh
```

### Your Endpoint

The script outputs: `http://lb-{id}.elb.localhost.localstack.cloud`

## Local Docker Development

```bash
# Start infrastructure
docker-compose up -d

# Build and run services
mvn clean package -DskipTests
java -jar auth-service/target/auth-service-*.jar
# ... other services
```

## Production AWS Deployment

```bash
# Configure AWS credentials
aws configure

# Deploy to AWS
cd infrastructure
# Remove BootstraplessSynthesizer from LocalStack.java
cdk deploy --profile production
```
