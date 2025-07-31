# Healthcare Microservices Platform

A comprehensive healthcare management system built with **Spring Boot microservices**, featuring JWT authentication, event-driven communication, and **AWS CDK infrastructure deployment**.

## 🚀 Quick Start

### LocalStack Deployment (Recommended)

```bash
# 1. Start LocalStack
localstack start -d

# 2. Build Docker images
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

**Your endpoint:** `http://lb-{id}.elb.localhost.localstack.cloud`

### Test Authentication

```bash
curl -X POST $ENDPOINT/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"testuser@test.com","password":"password123"}'
```

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Apps   │───▶│ Load Balancer   │───▶│   API Gateway   │
│                 │    │  (AWS ALB)      │    │   (Port 4004)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                       │
                                                       ▼
                                            ┌─────────────────┐
                                            │ JWT Validation  │
                                            │    Filter       │
                                            └─────────────────┘
                                                       │
                                                       ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                            ECS Fargate Services                                     │
├─────────────────┬─────────────────┬─────────────────┬─────────────────────────────┤
│  Auth Service   │ Patient Service │ Billing Service │    Analytics Service       │
│   (Port 4005)   │   (Port 4000)   │ (Ports 4001/    │      (Port 4002)           │
│                 │                 │      9001)      │                             │
└─────────────────┴─────────────────┴─────────────────┴─────────────────────────────┘
         │                   │                                          │
         ▼                   ▼                                          ▼
┌─────────────────┐ ┌─────────────────┐                    ┌─────────────────┐
│  PostgreSQL     │ │  PostgreSQL     │                    │   MSK Kafka     │
│ (Auth Service)  │ │(Patient Service)│                    │   Cluster       │
└─────────────────┘ └─────────────────┘                    └─────────────────┘
```

**5 Microservices** deployed on **AWS ECS Fargate** with:

- **API Gateway** (Spring Cloud Gateway) - Routing & JWT validation
- **Auth Service** - JWT authentication with PostgreSQL
- **Patient Service** - Patient management with Kafka events
- **Billing Service** - gRPC server for billing operations
- **Analytics Service** - Kafka consumer for event processing

**Infrastructure:** AWS CDK → ECS + MSK + RDS + ALB

### Event Flow

```
Patient Creation Flow:
┌─────────┐    ┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Client  │───▶│ API Gateway │───▶│ Patient Service │───▶│   PostgreSQL    │
└─────────┘    └─────────────┘    └─────────────────┘    └─────────────────┘
                                           │
                                           ▼
                                  ┌─────────────────┐
                                  │ Billing Service │ (gRPC)
                                  └─────────────────┘
                                           │
                                           ▼
                                  ┌─────────────────┐    ┌─────────────────┐
                                  │  Kafka Topic    │───▶│ Analytics       │
                                  │   (patient)     │    │   Service       │
                                  └─────────────────┘    └─────────────────┘
```

## 🔧 Tech Stack

**Backend:** Java 21, Spring Boot 3.5.3, Spring Security, JPA/Hibernate  
**Communication:** Kafka, gRPC, REST APIs, Protocol Buffers  
**Infrastructure:** AWS CDK, ECS Fargate, MSK, RDS PostgreSQL, ALB  
**Development:** Docker, LocalStack, Maven, JUnit 5

## 📚 Documentation

| Topic                 | Link                                               |
| --------------------- | -------------------------------------------------- |
| **Deployment Guide**  | [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)           |
| **API Documentation** | [docs/API.md](docs/API.md)                         |
| **Troubleshooting**   | [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) |

## 🧪 Testing

```bash
# Integration tests
cd integration-tests && mvn test

# Individual service tests
cd patient-service && mvn test
```

## ✅ Completed Features

- [x] **Microservices Architecture** - 5 independent services
- [x] **JWT Authentication** - Secure token-based auth with validation
- [x] **API Gateway** - Centralized routing and security
- [x] **Event-Driven Communication** - Kafka integration with Protobuf
- [x] **gRPC Communication** - High-performance inter-service calls
- [x] **Database Integration** - PostgreSQL with JPA/Hibernate
- [x] **Docker Containerization** - All services containerized
- [x] **Infrastructure as Code** - AWS CDK with Java
- [x] **LocalStack Deployment** - Complete AWS simulation locally
- [x] **Load Balancing** - Application Load Balancer with health checks
- [x] **Container Orchestration** - ECS Fargate services
- [x] **Managed Services** - MSK Kafka and RDS PostgreSQL
- [x] **Centralized Logging** - CloudWatch logs integration
- [x] **Integration Testing** - REST Assured test suite
- [x] **API Documentation** - Swagger/OpenAPI integration
- [x] **Health Monitoring** - Service health checks and monitoring

## 🔄 Upcoming Features

- [ ] **Service Mesh** - Istio integration for advanced traffic management
- [ ] **Distributed Tracing** - Jaeger/Zipkin for request tracing
- [ ] **Circuit Breaker** - Resilience4j for fault tolerance
- [ ] **Caching Layer** - Redis integration for performance
- [ ] **Security Enhancements** - OAuth2, rate limiting, API versioning
- [ ] **Kubernetes Deployment** - Helm charts and K8s manifests
- [ ] **CI/CD Pipeline** - GitHub Actions for automated deployment

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Add tests
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

---

**Built with ❤️ for modern healthcare management**
