INFRA_DIR := infrastructure/docker

up:
	docker compose -f $(INFRA_DIR)/compose.yaml up -d

down:
	docker compose -f $(INFRA_DIR)/compose.yaml down

restart:
	docker compose -f $(INFRA_DIR)/compose.yaml restart

logs:
	docker compose -f $(INFRA_DIR)/compose.yaml logs -f

ps:
	docker compose -f $(INFRA_DIR)/compose.yaml ps

kafka-topics:
	docker exec -it homepulse-kafka /opt/kafka/bin/kafka-topics.sh \
	--bootstrap-server localhost:9092 --list
build:
	./gradlew build

test:
	./gradlew clean test