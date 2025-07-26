# Healthcare Microservices Platform

A comprehensive healthcare management system built with Spring Boot microservices architecture, featuring JWT authentication, event-driven communication, and API Gateway routing.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Apps   │───▶│   API Gateway   │───▶│  Microservices  │
│                 │    │   (Port 4004)   │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │ JWT Validation  │
                    │    Filter       │
                    └─────────────────┘
```

## 🚀 Services

| Service               | Port | Description                      | Database   | Key Features                               |
| --------------------- | ---- | -------------------------------- | ---------- | ------------------------------------------ |
| **API Gateway**       | 4004 | Central routing & JWT validation | -          | Spring Cloud Gateway, Request routing      |
| **Auth Service**      | 4005 | Authentication & authorization   | PostgreSQL | JWT tokens, BCrypt encryption              |
| **Patient Service**   | 4000 | Patient management               | PostgreSQL | CRUD operations, Kafka events, gRPC client |
| **Billing Service**   | 4001 | Billing & payments               | -          | gRPC server, Account management            |
| **Analytics Service** | 4002 | Data analytics                   | -          | Kafka consumer, Event processing           |

## 🔧 Technology Stack

### Backend

- **Java 21** - Modern Java features
- **Spring Boot 3.5.3** - Microservices framework
- **Spring Cloud Gateway** - API Gateway & routing
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Primary database

### Communication

- **Apache Kafka** - Event streaming & messaging
- **gRPC** - High-performance RPC communication
- **REST APIs** - HTTP-based service communication
- **Protocol Buffers** - Efficient data serialization

### Infrastructure

- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Kafka UI** - Kafka cluster management

### Testing

- **JUnit 5** - Unit testing framework
- **REST Assured** - API integration testing
- **Testcontainers** - Integration testing with containers

## 🔐 Security

- **JWT Authentication** - Stateless token-based auth
- **BCrypt Password Encoding** - Secure password storage
- **API Gateway Security Filter** - Centralized auth validation
- **Role-based Access Control** - User permission management

## 📡 Event-Driven Architecture

```
Patient Service ──(Kafka)──▶ Analytics Service
      │                           │
      ▼                           ▼
 Patient Events              Event Processing
 (Protobuf)                  & Analytics
```

## 🚦 Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### 1. Clone Repository

```bash
git clone <repository-url>
cd healthcare-microservices
```

### 2. Start Infrastructure

```bash
# Start Kafka and databases
docker-compose up -d

# Start Kafka UI (optional)
# Access at http://localhost:8080
```

### 3. Build Services

```bash
# Build all services
mvn clean package -DskipTests

# Or build individual services
cd patient-service && mvn clean package -DskipTests
cd auth-service && mvn clean package -DskipTests
# ... repeat for other services
```

### 4. Run Services

```bash
# Start each service (in separate terminals or as Docker containers)
java -jar auth-service/target/auth-service-*.jar
java -jar patient-service/target/patient-service-*.jar
java -jar billing-service/target/billing-service-*.jar
java -jar analytics-service/target/analytics-service-*.jar
java -jar api-gateway/target/api-gateway-*.jar
```

## 📚 API Documentation

### Authentication

```bash
# Login
POST http://localhost:4004/auth/login
Content-Type: application/json

{
  "email": "testuser@test.com",
  "password": "password123"
}

# Response
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Patient Management (Protected)

```bash
# Create Patient
POST http://localhost:4004/api/patients
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phone": "123-456-7890"
}
```

### API Documentation URLs

- Auth Service: http://localhost:4004/api-docs/auth
- Patient Service: http://localhost:4004/api-docs/patients

## 🧪 Testing

### Integration Tests

```bash
cd integration-tests
mvn test
```

### Individual Service Tests

```bash
cd patient-service
mvn test
```

## 🔄 Event Flow

1. **Patient Creation**:

   - Client → API Gateway → Patient Service
   - Patient Service → Database (save patient)
   - Patient Service → Billing Service (gRPC call)
   - Patient Service → Kafka (publish event)
   - Analytics Service ← Kafka (consume event)

2. **Authentication Flow**:
   - Client → API Gateway → Auth Service
   - Auth Service → Database (validate credentials)
   - Auth Service → Client (JWT token)
   - Subsequent requests use JWT for validation

## 🐳 Docker Configuration

### Networks

- **internal**: Inter-service communication network

### Containers

- `kafka`: Apache Kafka message broker
- `kafka-ui`: Web UI for Kafka management
- `auth-service-db`: PostgreSQL for auth service
- `patient-service-db`: PostgreSQL for patient service

## 📊 Monitoring & Management

- **Kafka UI**: http://localhost:8080 - Monitor Kafka topics and messages
- **Health Endpoints**: Each service exposes `/actuator/health`
- **Logs**: Docker logs available via `docker logs <container-name>`

## 🔧 Configuration

### Environment Variables

Services can be configured via environment variables:

- `SPRING_DATASOURCE_URL`: Database connection
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker address
- `AUTH_SERVICE_URL`: Auth service endpoint

### Application Properties

Each service has its own `application.properties` for configuration.

## 🚀 Deployment

### Local Development

Use the provided Docker Compose setup for local development.

### Production

- Configure external databases
- Set up Kafka cluster
- Use container orchestration (Kubernetes/Docker Swarm)
- Configure load balancers
- Set up monitoring and logging

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 🆘 Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 4000-4005, 8080, 9092 are available
2. **Database connection**: Check PostgreSQL containers are running
3. **Kafka connectivity**: Verify Kafka container is healthy
4. **JWT validation**: Ensure auth-service is accessible from API Gateway

### Useful Commands

```bash
# Check running containers
docker ps

# View service logs
docker logs <service-name>

# Restart API Gateway (fixes DNS issues)
docker restart api-gateway

# Test Kafka connectivity
docker exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092
```

---

**Built with ❤️ for modern healthcare management**
