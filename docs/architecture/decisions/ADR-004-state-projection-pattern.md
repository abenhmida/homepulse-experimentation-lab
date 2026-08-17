# ADR-004 — State Projection Pattern

## Status

Accepted

## Context

The system receives a stream of fine-grained device events (e.g., `DeviceTemperatureReported`). To serve user queries or evaluate automation rules efficiently, we need a "current snapshot" of each device's state.

Calculating this state from the entire event history every time a query is made would be too slow and expensive.

## Decision

Use the **State Projection** pattern.

1. A dedicated `State Service` consumes events from Kafka.
2. It uses a `Projector` component to transform domain events into a `DeviceState` model.
3. The projected state is persisted in a materialized view (DynamoDB).

## Rationale

- **Performance**: Reads are optimized since the state is pre-calculated.
- **Decoupling**: The state model can evolve independently of the event model.
- **Simplicity**: Consumers of the state (e.g., a Mobile App API) only need to query a single record rather than aggregating events.

## Consequences

### Positive

- High-performance reads for device status.
- Clear separation between "what happened" (events) and "what is the current status" (state).

### Negative

- Eventual consistency: there is a small delay between an event occurring and the state being updated.
- Requires extra storage and compute for the projection process.
