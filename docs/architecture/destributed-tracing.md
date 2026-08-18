# Distributed tracing

## Objective

Trace one HomePulse event across:

```text
producer -> Kafka -> consumer -> projection -> DynamoDB
```

## Trace context

OpenTelemetry propagates W3C Trace Context using Kafka headers:

```text
traceparent
tracestate
```

Business identifiers remain in the event:

```text
eventId
correlationId
deviceId
```

## Why both?

`eventId` answers:

> Which business event is this?

`correlationId` answers:

> Which business workflow does it belong to?

`traceId` answers:

> Which distributed execution path does this telemetry belong to?

Do not collapse these concepts into one ID.

## Important event-driven distinction

A trace does not eliminate the asynchronous nature of Kafka.

Measure separately:

- producer publish latency
- Kafka consumer processing duration
- event age
- Kafka consumer lag
- retry delay
- DLQ delay

A consumer can process an event in 10 ms while the event is five minutes old
because it waited in Kafka or retry infrastructure.

## Failure experiments

### Collector failure

Stop the Collector:

```bash
docker stop homepulse-otel
```

The business path should remain functional.

### DynamoDB latency

Introduce a deliberate delay in the repository and observe the child span.

### Consumer latency

Add a delay immediately before projection and compare:

- event age
- processing duration
- Kafka lag

### Broken propagation

Remove `KafkaPropagation.inject(...)` from the producer and observe how the
consumer trace becomes disconnected.

## Senior-level lesson

Observability is part of the architecture, but it must remain a non-blocking
dependency of the business path.
