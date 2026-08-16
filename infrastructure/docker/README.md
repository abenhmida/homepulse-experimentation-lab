# HomePulse Local Infrastructure

## Services

| Service         | Host | Container |
|-----------------|-----:|----------:|
| Kafka           | 9092 |     29092 |
| DynamoDB        | 8000 |      8000 |
| Schema Registry | 8081 |      8081 |
| Prometheus      | 9090 |      9090 |
| Grafana         | 3000 |      3000 |

## Start

```bash
docker compose up -d
```
or 
```bash
make up
```

## Stop
```bash
docker compose down
```
or
```bash
make down
```

## Logs
```bash
docker compose logs -f
```
or
```bash
make logs
```

## Kafka list topics
```bash
docker exec homepulse-kafka \
  /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092
  --list
```
## Dynamodb list tables
```bash
aws dynamodb list-tables \
--endpoint-url http://localhost:8000 \
--region eu-west-3
```