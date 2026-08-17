# ADR-003 — Idempotent Event Processing

## Status

Accepted

## Context

In an event-driven system using Kafka, "at-least-once" delivery is the standard. This means consumers may receive the same event multiple times due to network retries, rebalances, or producer retries.

HomePulse must ensure that processing the same event twice does not lead to inconsistent state (e.g., applying an old temperature report over a newer one, or double-counting an event).

## Decision

Implement idempotency at the database level using a combination of:

1. **Idempotency Marker**: Store the `eventId` in the `homepulse` table as part of a transaction when updating state.
2. **Conditional Writes**: Use DynamoDB's `attribute_not_exists(PK)` on the idempotency record to fail the transaction if the event has already been processed.
3. **Sequence Checking**: Use a `lastSequenceNumber` check to ensure that even if an event is processed, it only updates state if it is newer than the current state (versioning).

## Rationale

- **Accuracy**: Guarantees that the "current state" reflects the most recent intent, regardless of delivery order or duplicates.
- **Atomicity**: By bundling the state update and the idempotency marker in a single DynamoDB transaction, we ensure that either both succeed or both fail, preventing partial updates.

## Consequences

### Positive

- Robustness against Kafka retries and duplicate events.
- Simplified consumer logic (doesn't need to be perfectly "once-only").

### Negative

- Increased write cost in DynamoDB due to the extra idempotency item and transaction overhead.
- Slight increase in latency for write operations.
