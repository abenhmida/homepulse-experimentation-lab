```bash
docker exec homepulse-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic home.device.retry \
  --partitions 6 \
  --replication-factor 1
```

```bash
docker exec homepulse-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic home.device.dlq \
  --partitions 6 \
  --replication-factor 1
```

```bash
docker exec homepulse-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic home.events.dlq.v1 \
  --partitions 6 \
  --replication-factor 1
```