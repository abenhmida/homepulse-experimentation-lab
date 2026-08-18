# ADR-008 — Distributed Tracing

## Decision

HomePulse uses OpenTelemetry for distributed tracing.

Tracing context is propagated through Kafka headers using
the W3C Trace Context format.

## Principles

- business event IDs remain independent from trace IDs
- tracing metadata is transported through Kafka headers
- telemetry failures must not stop event processing
- high-cardinality identifiers belong primarily in traces/logs
- metrics use bounded-cardinality labels
- Kafka processing remains asynchronous
- retries and DLQ operations are observable

## Components

- OpenTelemetry SDK
- OpenTelemetry Collector
- Prometheus
- Grafana
- trace backend

## Rationale

Distributed tracing allows engineers to identify latency and
failure boundaries across asynchronous event-driven workflows.