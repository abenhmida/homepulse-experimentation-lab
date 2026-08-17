# ADR-006 — Observability Architecture

## Decision

HomePulse uses:

- Micrometer for application metrics
- Prometheus for metrics collection
- Grafana for visualization
- structured JSON logging
- OpenTelemetry for distributed tracing

## Rationale

The system is event-driven and distributed.
Infrastructure-level metrics alone are insufficient.

We need visibility into:

- Kafka
- state projection
- DynamoDB
- event latency
- business outcomes