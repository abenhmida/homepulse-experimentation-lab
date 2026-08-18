#!/usr/bin/env bash

set -euo pipefail

docker compose up -d

echo "WWaiting for DynamoDB..."

until curl -fsS http://localhost:8000/shell/ > /dev/null 2>&1; do
  sleep 1
done || true

./infrastructure/dynamodb/create-tables.sh

echo
echo "HomePulse lab started."
echo "Kafka:       localhost:9092"
echo "DynamoDB:    localhost:8000"
echo "Jaeger:      http://localhost:16686"
echo "Prometheus:  http://localhost:9090"
echo "Grafana:     http://localhost:3000"
