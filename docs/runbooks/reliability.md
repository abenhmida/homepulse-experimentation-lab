# Reliability Baseline

## Completed

- Application-neutral `RetryMessagePublisher` boundary.
- Kafka retry/DLQ publisher adapter.
- Correct synchronous Kafka publication semantics: `publish()` returns only after broker acknowledgement or throws.
- Retry policy distinguishes retryable and permanent failures.
- Initial failures create retry attempt 1.
- Retry attempts advance 1 -> 2 -> 3 ... and exhaust into DLQ.
- Retry backoff and jitter determine `nextAttemptAt`.
- Retry metadata preserves original event ID/topic/partition/offset.
- Retry consumer uses manual acknowledgement.
- Main event consumer creates the first retry/DLQ envelope.
- Retry consumer processes retry envelopes through the same `StateService` projection path.
- Event payload deserialization is selected by canonical event type.
- Duplicate and stale projections are terminal successes for Kafka acknowledgement.
- Publisher failures propagate and prevent acknowledgement.
- Local Kafka topic topology is aligned with application configuration.
- DynamoDB Local table definitions are aligned with the actual repository key schema.
- Removed unused `EventProcessor` and `RetryMetadataFactory` abstractions.
- Added Step J deterministic failure tests.
- Added a local crash-before-ack fault injection hook, disabled by default.

## Failure contract

```text
SUCCESS
  -> ACK

RETRYABLE FAILURE
  -> publish retry
  -> ACK only after successful publication

PERMANENT FAILURE
  -> publish DLQ
  -> ACK only after successful publication

PUBLISH FAILURE
  -> exception
  -> NO ACK
  -> Kafka redelivery

CRASH AFTER PUBLICATION / BEFORE ACK
  -> duplicate delivery is expected
  -> DynamoDB idempotency protects the projection
```

## Verification

Static consistency checks were performed against the complete uploaded repository.
YAML files were parsed successfully.

The Gradle wrapper could not be executed in this environment because the Gradle 9.5.1 distribution was not cached and external access to `services.gradle.org` is unavailable.

```bash
./gradlew clean test
./gradlew :applications:state-service:test
```

Then run the local failure laboratory:

```bash
./scripts/start-lab.sh
./scripts/run-state-service.sh
./scripts/run-event-producer.sh
```

Finally execute the crash experiment with:

```bash
HOMEPULSE_CHAOS_CRASH_BEFORE_ACK=true ./scripts/run-state-service.sh
```

Disable the flag after the experiment.
