# Local Infrastructure Architecture

## Objective

Provide a reproducible local distributed-systems environment for HomePulse.

## Components

- Apache Kafka
- Schema Registry
- DynamoDB Local
- Prometheus
- Grafana

## Network

All infrastructure services communicate through the `homepulse`
Docker network.

## Kafka

Kafka uses KRaft mode.

Internal applications use:

    kafka:29092

Host applications use:

    localhost:9092

## Persistence

DynamoDB Local stores state under a Docker volume.

## Observability

Prometheus scrapes metrics.

Grafana queries Prometheus.

## Design Principle

The application must not depend on Docker-specific hostnames.

Infrastructure endpoints are injected through configuration.