# Reliability model

HomePulse uses several independent reliability mechanisms.

## Producer

```text
acks=all
enable.idempotence=true
retries=large
```

This protects the producer-to-Kafka path.

## Consumer

Kafka offsets are committed after processing:

```text
poll
  |
process
  |
commit
```

This means a crash before commit can cause redelivery.

Therefore the projection must be idempotent.

## Idempotency

DynamoDB uses a conditional write:

```text
attribute_not_exists(eventId)
```

The first event wins.

A duplicate is treated as:

```text
DUPLICATE
```

rather than a processing failure.

## Retry

Failures are published to a retry topic.

A production design should add:

- attempt count
- first failure timestamp
- next retry timestamp
- original topic
- original partition
- original offset
- error class
- error message hash
- retry bucket

## DLQ

Permanent failures are sent to a DLQ.

DLQ is not a garbage can. It is an operational workflow.

Every DLQ record should be diagnosable and replayable.
