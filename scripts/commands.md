# HomePulse commands

## Kafka topics

```bash
docker exec homepulse-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 --list
```

## Consume the main event stream

```bash
docker exec -it homepulse-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic home.events \
  --from-beginning
```

## Consume retry tier 1

```bash
docker exec -it homepulse-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic home.events.retry.1 \
  --from-beginning
```

## Consume the DLQ

```bash
docker exec -it homepulse-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic home.events.dlq \
  --from-beginning
```

## DynamoDB tables

```bash
aws dynamodb list-tables \
  --endpoint-url http://localhost:8000 \
  --region eu-west-3
```
