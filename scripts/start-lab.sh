#!/usr/bin/env bash

set -euo pipefail

docker compose up -d

echo "Waiting for DynamoDB..."

until curl -fsS http://localhost:8000/shell/ > /dev/null 2>&1; do
  sleep 1
done || true

./infrastructure/docker/dynamodb/create-tables.sh

echo
echo "HomePulse lab started."
echo "Kafka:       localhost:9092"
echo "DynamoDB:    localhost:8000"
echo "OTLP:        localhost:4317 / localhost:4318"
echo "Prometheus:  http://localhost:9090"
echo "Grafana:     http://localhost:3000"
