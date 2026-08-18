# ADR-007 — Retry and Dead Letter Architecture

## Decision

HomePulse uses:

- at-least-once Kafka processing
- explicit failure classification
- retry topics
- exponential backoff
- jitter
- bounded retry attempts
- dead-letter topic
- DynamoDB idempotency
- structured retry metadata

## Failure categories

### Expected

- duplicate
- stale

### Transient

- throttling
- timeout
- temporary infrastructure failure

### Permanent

- malformed event
- unsupported schema
- invalid domain data

## Rationale

The system must avoid:

- retry storms
- infinite retries
- message loss
- poison-message blocking