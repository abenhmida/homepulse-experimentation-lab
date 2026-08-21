# Retry Publisher

## Responsibility

The retry publisher is the transport adapter between the state-service retry application port and Kafka.

```text
state-service application
        |
        | RetryMessagePublisher
        v
KafkaRetryMessagePublisher
        |
        +--> KafkaRetryTopicStrategy
        +--> KafkaRetryHeaderMapper
        +--> JsonMapper
        |
        v
EventPublisher
        |
        v
KafkaProducer
```

## Contract

`RetryMessagePublisher` exposes only two operations:

- `publishRetry(envelope)`
- `publishDeadLetter(envelope)`

It deliberately does not expose Kafka topics, `ProducerRecord`, Kafka headers, or producer configuration.

## Retry topic mapping

For an original topic `home.events`:

- attempt 1 -> `home.events.retry.1`
- attempt 2 -> `home.events.retry.2`
- attempt 3 -> `home.events.retry.3`
- dead letter -> `home.events.dlq`

The mapping is owned by `KafkaRetryTopicStrategy` in `libraries:kafka-common`.

## Partition key

The publisher uses the existing HomePulse partitioning decision:

```text
homeId:deviceId
```

This preserves per-device ordering across the original and retry publications.

## Headers

The publisher sends the normal event headers plus the retry metadata headers. The event type is supplied through `PublishedEvent.eventType` and is therefore added by the shared Kafka publisher exactly once.

## Failure semantics

`KafkaRetryMessagePublisher` does not swallow producer exceptions. A failed `EventPublisher.publish` propagates to the caller. This is intentional: the eventual consumer/processing layer must not acknowledge the original Kafka record when retry publication has failed.

## Testing

`KafkaRetryMessagePublisherTest` verifies:

- retry topic selection;
- DLQ topic selection;
- partition key;
- retry headers;
- correlation/causation propagation;
- envelope serialization;
- transport failure propagation.
