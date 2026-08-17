# LAB-01 Producer outage
## Hypothesis
## Setup
## Expected behavior
## Observed behavior
## Metrics
## Root cause
## Architecture lesson


# LAB-02 Consumer crash
## Objective
Demonstrate at-least-once processing.

## Setup
1. Start Kafka.
2. Start state-service.
3. Start device simulator.

## Failure
Kill state-service while processing an event.

## Expected
The event should be consumed again after restart.

## Observation
Record:

- eventId
- partition
- offset
- committed offset

## Lesson
Kafka consumers must tolerate replay.

## Architecture implication
Business processing must be idempotent.


# LAB-03 Duplicate event
## Hypothesis
## Setup
## Expected behavior
## Observed behavior
## Metrics
## Root cause
## Architecture lesson


# LAB-04 Poison message
## Hypothesis
## Setup
## Expected behavior
## Observed behavior
## Metrics
## Root cause
## Architecture lesson


# LAB-05 Retry exhaustion
## Hypothesis
## Setup
## Expected behavior
## Observed behavior
## Metrics
## Root cause
## Architecture lesson


# LAB-06 DLQ
## Hypothesis
## Setup
## Expected behavior
## Observed behavior
## Metrics
## Root cause
## Architecture lesson


# LAB-07 Consumer lag
## Hypothesis
## Setup
## Expected behavior
## Observed behavior
## Metrics
## Root cause
## Architecture lesson


# LAB-08 Rebalance
## Hypothesis
## Setup
## Expected behavior
## Observed behavior
## Metrics
## Root cause
## Architecture lesson


# LAB-09 Hot partition
## Hypothesis
## Setup
## Expected behavior
## Observed behavior
## Metrics
## Root cause
## Architecture lesson


# LAB-10 Database outage
## Hypothesis
## Setup
## Expected behavior
## Observed behavior
## Metrics
## Root cause
## Architecture lesson
