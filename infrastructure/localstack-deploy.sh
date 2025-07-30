#!/bin/zsh

set -e

# Delete existing failed stack first
echo "Deleting existing stack..."
aws --endpoint-url=http://localhost:4566 cloudformation delete-stack \
    --stack-name healthcare-microservices || true

# Wait a moment for deletion
sleep 5

# Deploy the stack
echo "Deploying stack..."
aws --endpoint-url=http://localhost:4566 cloudformation deploy \
    --stack-name healthcare-microservices \
    --template-file "./cdk.out/localstack.template.json"

# Get load balancer DNS
echo "🎯 Your API Gateway endpoint:"
ENDPOINT=$(aws --endpoint-url=http://localhost:4566 elbv2 describe-load-balancers \
    --query "LoadBalancers[0].DNSName" --output text)
echo "http://$ENDPOINT"



