# Phase 1.11.4 — Step J: Crash & Recovery Laboratory

This laboratory proves the reliability contract of HomePulse before the system is moved to AWS.

## Reliability invariants

1. A Kafka message is acknowledged only after processing succeeds, a retry message is published successfully, or a DLQ message is published successfully.
2. A publisher failure propagates and prevents acknowledgement.
3. DynamoDB projection and idempotency writes are atomic.
4. Duplicate delivery is a terminal success for the consumer.
5. A retryable failure is retried only while the retry budget remains.
6. Permanent failures and exhausted retry budgets go to the DLQ.
7. Retry metadata preserves the original event identity and transport location.
8. Retry delays are represented by `nextAttemptAt`; a message that arrives too early is not processed.
9. Per-device ordering is preserved by the `homeId:deviceId` partition key.

## Failure matrix

| Failure point | Expected behavior |
|---|---|
| Before processing | Kafka redelivery |
| During state projection | Retry/DLQ according to classification |
| After DynamoDB commit | Duplicate delivery is harmless |
| Retry publication fails | No ACK |
| DLQ publication fails | No ACK |
| Retry publication succeeds, process crashes before ACK | Original is redelivered; idempotency protects state |
| Retry budget exhausted | DLQ |
| Permanent failure | DLQ |
| Retry arrives before `nextAttemptAt` | No processing; no ACK |
| Kafka unavailable | Producer failure propagates |
| DynamoDB unavailable | Classified as retryable |

## Manual laboratory

### 1. Start local infrastructure

```bash
./scripts/start-lab.sh
```

### 2. Verify topics

```bash
docker exec homepulse-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --list
```

Expected retry topology:

```text
home.events
home.events.retry.1
home.events.retry.2
home.events.retry.3
home.events.dlq
```

### 3. Start the state service

```bash
./scripts/run-state-service.sh
```

### 4. Start the simulator

```bash
./scripts/run-event-producer.sh
```

### 5. Observe retry and DLQ traffic

```bash
docker exec -it homepulse-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic home.events.retry.1 \
  --from-beginning
```

```bash
docker exec -it homepulse-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic home.events.dlq \
  --from-beginning
```

## Chaos experiments

### Experiment A — DynamoDB outage

1. Stop DynamoDB Local:

```bash
docker stop homepulse-dynamodb
```

2. Publish/process an event.
3. Observe `DynamoDB` failures classified as retryable.
4. Verify the consumer does not acknowledge the failed message.
5. Restart DynamoDB:

```bash
docker start homepulse-dynamodb
```

6. Verify recovery and eventual projection.

### Experiment B — Kafka outage during retry publication

Stop Kafka:

```bash
docker stop homepulse-kafka
```

A retry publication must fail. The original message must not be acknowledged.

Restart:

```bash
docker start homepulse-kafka
```

### Experiment C — Crash before ACK

The intended crash window is:

```text
process failure
    -> publish retry successfully
    -> crash
    -> no ACK
```

On restart Kafka redelivers the original record. The retry publication may therefore happen again. This is expected at-least-once behavior.

Verify the projection remains correct because DynamoDB idempotency prevents duplicate application of the same event ID.

### Experiment D — Retry exhaustion

Configure:

```yaml
homepulse:
  retry:
    max-attempts: 3
```

Force a retryable failure repeatedly.

Expected sequence:

```text
attempt 1 -> retry.2
attempt 2 -> retry.3
attempt 3 -> DLQ
```

There must never be a retry.4 when `max-attempts` is 3.

### Experiment E — Poison message

Inject an event whose payload cannot be accepted by the domain projector.

Expected:

```text
invalid payload
    -> FailureClassifier
    -> PERMANENT
    -> DLQ
```

A poison message must not consume retry capacity indefinitely.

## Operational evidence

During the experiments observe:

- Kafka consumer lag
- retry topic depth
- DLQ depth
- state projection success/failure
- DynamoDB errors
- retry attempt distribution
- processing latency
- producer errors

Prometheus endpoint:

```text
http://localhost:8080/actuator/prometheus
```

Grafana:

```text
http://localhost:3000
```

## Exit criteria

Phase 1 is considered complete only when:

- unit tests pass;
- the state service starts successfully;
- local Kafka and DynamoDB are reachable;
- retry publication is acknowledged only after successful Kafka publication;
- permanent failures reach the DLQ;
- retry exhaustion reaches the DLQ;
- duplicate delivery does not corrupt state;
- a Kafka outage does not cause silent loss;
- a DynamoDB outage causes retryable behavior;
- the crash-before-ACK scenario is understood and demonstrated.

### Experiment F — Crash immediately before ACK

The listeners contain a disabled-by-default fault-injection hook.

Start the state service with:

```bash
HOMEPULSE_CHAOS_CRASH_BEFORE_ACK=true ./scripts/run-state-service.sh
```

The first successful processing path that reaches an acknowledgement will throw immediately before the acknowledgement call.

For the retry path, the sequence becomes:

```text
retry.1
  -> process fails
  -> publish retry.2 successfully
  -> simulated crash
  -> NO ACK
  -> Kafka redelivery
```

Disable the flag before restarting the service normally:

```bash
./scripts/run-state-service.sh
```

The hook is intentionally process-local and disabled by default. It is a laboratory mechanism, not a production feature.
