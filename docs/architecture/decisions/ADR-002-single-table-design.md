# ADR-002 — Single Table Design for DynamoDB

## Status

Accepted

## Context

HomePulse needs a scalable, low-latency data store for device state, idempotency records, and metadata. We are using DynamoDB.

Traditional relational modeling would lead to multiple tables (Devices, Homes, IdempotencyKeys) and multiple requests per operation.

## Decision

Use a Single Table Design approach in DynamoDB.

- **Table Name**: `homepulse`
- **Primary Key**:
    - Partition Key (PK): String
    - Sort Key (SK): String

### Partitioning Strategy

- **Device State**: 
    - PK: `HOME#{homeId}`
    - SK: `DEVICE#{deviceId}`
- **Idempotency**:
    - PK: `IDEMPOTENCY#{context}`
    - SK: `EVENT#{eventId}`

## Rationale

1. **Atomic Transactions**: Single table design allows us to perform atomic updates across different entity types (e.g., updating device state and recording an idempotency key) using `TransactWriteItems`.
2. **Efficiency**: Fetches related data in a single query by leveraging the Sort Key.
3. **Scalability**: DynamoDB handles horizontal scaling transparently based on PK distribution.

## Consequences

### Positive

- Strong consistency for related entities within the same transaction.
- Reduced number of round-trips to the database.
- Flexible schema within the table.

### Negative

- More complex query patterns compared to relational databases.
- Overloading PK/SK requires careful naming and documentation.
- Risk of "hot partitions" if a single home has an extreme volume of events.
