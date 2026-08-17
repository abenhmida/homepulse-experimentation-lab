# DynamoDB Failure Runbook

## Symptoms

- projection failures increasing
- Kafka consumer lag increasing
- event processing throughput decreasing

## Verify

Check:

- DynamoDB availability
- state-service logs
- DynamoDB latency
- DynamoDB error counters
- Kafka consumer lag

## Recovery

1. Verify DynamoDB.
2. Restore connectivity.
3. Verify projection resumes.
4. Verify Kafka lag decreases.
5. Confirm no state corruption.

## Validation

Check:

- latest sequence number
- latest event ID
- projection metrics
- duplicate count