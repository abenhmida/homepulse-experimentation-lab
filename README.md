# HomePulse: Event-Driven Architecture Lab

HomePulse is a laboratory project designed to study and demonstrate **Event-Driven Architecture (EDA)** patterns using **Apache Kafka**, **Kotlin**, and **Spring Boot**. It simulates a smart home ecosystem where various devices emit telemetry and respond to commands, focusing on reliability, scalability, and observability.

## 🎯 Purpose

The primary goal of this project is to provide a hands-on environment for exploring:
- **At-least-once delivery** and handling idempotency.
- **State Projection** patterns (Materialized Views).
- **Single Table Design** with NoSQL (DynamoDB).
- **Event Partitioning** and ordering guarantees.
- **Observability** in distributed systems.
- **Chaos engineering** in event-driven environments.

## 🏗️ General Architecture

HomePulse follows a microservices architecture centered around a Kafka event backbone.

- **Event Gateway**: The entry point for external events and commands.
- **State Service**: A Spring Boot-based service that consumes raw events and projects them into a queryable device state.
- **Device Simulator**: Simulates a fleet of smart home devices (sensors, lights, etc.) generating traffic.
- **Infrastructure**: Local development environment using Docker Compose (Kafka, DynamoDB Local, Prometheus, Grafana).

### Architecture Decisions (ADRs)
The project's design is documented through Architectural Decision Records in `docs/architecture/decisions/`:
- **ADR-001**: Event-Driven Communication.
- **ADR-002**: Single Table Design for DynamoDB.
- **ADR-003**: Idempotent Event Processing.
- **ADR-004**: State Projection Pattern.
- **ADR-005**: Event Partitioning Strategy.
- **ADR-006**: Observability Stack.

## 🛠️ Technologies Used

- **Languages**: [Kotlin](https://kotlinlang.org/) (Coroutines, Serialization).
- **Messaging**: [Apache Kafka](https://kafka.apache.org/).
- **Frameworks**: [Spring Boot](https://spring.io/projects/spring-boot) (State Service), Gradle.
- **Persistence**: [Amazon DynamoDB](https://aws.amazon.com/dynamodb/) (via DynamoDB Local).
- **Observability**: [Micrometer](https://micrometer.io/), [Prometheus](https://prometheus.io/), [Grafana](https://grafana.com/).
- **Infrastructure**: [Docker](https://www.docker.com/), [Terraform](https://www.terraform.io/) (for local resource provisioning).

## 📂 Project Structure

```text
├── applications/             # Microservices
│   ├── device-simulator/    # Simulates device behavior
│   ├── event-gateway/      # Kafka producer gateway
│   └── state-service/       # Spring Boot-based consumer and projector
├── libraries/               # Shared logic
│   ├── event-model/         # Domain events, commands, and partitioning logic
│   └── kafka-common/        # Kafka clients wrappers and retry policies
├── infrastructure/          # Local environment setup
│   ├── docker/              # Compose files for Kafka, DynamoDB, Grafana
│   └── terraform/           # LocalStack/Local DynamoDB resource definitions
├── docs/                    # Architecture documentation and lab guides
└── scripts/                 # Chaos engineering and helper scripts
```

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21+ (for building)

### Running the Lab
1. **Start Infrastructure**:
   ```bash
   make up
   ```
   *This starts Kafka, DynamoDB Local, Prometheus, and Grafana.*

2. **Provision Resources**:
   ```bash
   # Run terraform to create DynamoDB tables
   cd infrastructure/terraform/local && terraform init && terraform apply
   ```

3. **Run Services**:
   Use Gradle to run individual services or the simulator:
   ```bash
   ./gradlew :applications:device-simulator:run
   ./gradlew :applications:state-service:bootRun
   ```

## 📈 Observability
Once the project is running, you can access:
- **Grafana**: `http://localhost:3000` (HomePulse Overview Dashboard)
- **Prometheus**: `http://localhost:9090`
- **Spring Boot Metrics**: `http://localhost:8080/actuator/prometheus`

## 🧪 Experiments
Check `docs/labs/` for guided experiments, such as:
- Testing Kafka reliability during broker failure.
- Verifying idempotency during consumer restarts.
