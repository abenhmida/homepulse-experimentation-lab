# ADR-001 — Event-driven communication
## Decision
Services communicate asynchronously through Kafka for domain events.

## Why
* decoupling
* independent scaling
* replay
* multiple consumers
* temporal decoupling
* event history
* failure isolation

## Trade-offs
* eventual consistency
* operational complexity
* duplicate processing
* ordering challenges
* debugging complexity