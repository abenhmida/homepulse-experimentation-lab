# ADR-005 — Event Partitioning Strategy

## Status

Accepted

## Context

Kafka provides ordering guarantees only within a single partition. In HomePulse, events from a single device (e.g., temperature updates) must be processed in the order they were produced to ensure the state reflects reality (newer events should not be overwritten by older ones).

## Decision

Use a composite key for Kafka partitioning.

- **Partition Key**: `homeId:deviceId`

This ensures that all events for a specific device in a specific home are always sent to the same Kafka partition.

## Rationale

- **Ordering Guarantee**: By using the device identity as the partition key, we guarantee that a single consumer instance will receive all events for that device in their original produced order.
- **Scalability**: Events are distributed across partitions based on the hash of the key. Since we have many devices across many homes, this provides a good distribution of load across the Kafka cluster.

## Consequences

### Positive

- Strict ordering for device-level events.
- Easy to scale by increasing the number of partitions and consumer instances.

### Negative

- "Hot devices" (devices sending extremely high frequency events) will load a single partition, which could lead to lag if a single consumer cannot keep up.
- Changing the partitioning strategy later is difficult as it requires re-keying or handling out-of-order events during transition.
