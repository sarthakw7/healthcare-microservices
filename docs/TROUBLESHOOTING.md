# Troubleshooting Guide

## LocalStack Issues

### Stack Creation Failed

```bash
# Check detailed error
aws --endpoint-url=http://localhost:4566 cloudformation describe-stack-events --stack-name healthcare-microservices

# Delete and retry
aws --endpoint-url=http://localhost:4566 cloudformation delete-stack --stack-name healthcare-microservices
./localstack-deploy.sh
```

### Service Not Responding

```bash
# Check ECS services
aws --endpoint-url=http://localhost:4566 ecs list-services --cluster PatientManagementCluster

# Check logs
aws --endpoint-url=http://localhost:4566 logs get-log-events --log-group-name "/ecs/auth-service:latest"
```

### Database Connection Issues

```bash
# Verify RDS instances
aws --endpoint-url=http://localhost:4566 rds describe-db-instances

# Check connection strings in service logs
```

## Local Development Issues

### Port Conflicts

- Ensure ports 4000-4005, 8080, 9092 are available
- Use `lsof -i :PORT` to check port usage

### Database Connection

```bash
# Check PostgreSQL containers
docker ps | grep postgres
docker logs auth-service-db
```

### Kafka Issues

```bash
# Check Kafka health
docker exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# Restart Kafka UI
docker restart kafka-ui
```

## Common Solutions

### DNS Resolution (LocalStack)

```bash
# Add to /etc/hosts if needed
echo "127.0.0.1 lb-{id}.elb.localhost.localstack.cloud" | sudo tee -a /etc/hosts
```

### Service Discovery

```bash
# Restart API Gateway for DNS refresh
docker restart api-gateway
```

### Test Connectivity

```bash
# Test auth endpoint
curl -X POST http://localhost:4566/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"testuser@test.com","password":"password123"}'
```
