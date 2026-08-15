# ADR-001 — Event-Driven Communication

## Status

Accepted

## Context

HomePulse contains multiple independently evolving components:

- device ingestion
- state management
- automation
- security
- notification
- analytics

A device event may be relevant to multiple consumers.

Direct synchronous communication between every component would create
strong temporal and availability coupling.

For example:

Device Gateway -> State Service

would mean that the State Service must be available whenever the Gateway
needs to process an event.

The architecture also needs the ability to replay historical events and
introduce new consumers without modifying existing producers.

## Decision

Use Apache Kafka as the primary event backbone for asynchronous domain
events.

The initial event flow is:

Device
|
v
Event Gateway
|
v
Kafka
|
+--> State Service
|
+--> Automation Service
|
+--> Security Service

## Consequences

### Positive

- Consumers are independently scalable.
- Producers do not need to know all consumers.
- Events can be replayed.
- New consumers can be introduced.
- Temporary consumer outages do not necessarily stop producers.
- Event history can support debugging and reconstruction.

### Negative

- The system becomes eventually consistent.
- Duplicate event processing must be handled.
- Ordering becomes a partitioning concern.
- Kafka introduces operational complexity.
- Debugging distributed asynchronous workflows is more difficult.

## Alternatives considered

### REST

Simple request/response communication.

Rejected as the primary mechanism for domain event propagation because it
creates temporal coupling and does not naturally provide durable event
replay.

### Synchronous messaging

Rejected because the domain requires multiple independent consumers and
durable event history.

## Notes

Kafka does not eliminate distributed-system problems. It moves the system
toward asynchronous communication while introducing new concerns such as
partitioning, consumer groups, offsets, rebalancing and idempotency.