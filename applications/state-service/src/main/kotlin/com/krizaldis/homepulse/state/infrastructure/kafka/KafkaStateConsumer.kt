package com.krizaldis.homepulse.state.infrastructure.kafka

import com.krizaldis.homepulse.kafka.Environment
import com.krizaldis.homepulse.kafka.KafkaConsumerFactory
import com.krizaldis.homepulse.kafka.KafkaEventConsumer
import com.krizaldis.homepulse.kafka.KafkaProperties
import com.krizaldis.homepulse.state.infrastructure.dynamodb.DynamoDbStateRepository
import com.krizaldis.homepulse.state.observability.StateMetrics
import io.micrometer.core.instrument.MeterRegistry
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KafkaStateConsumer(
    private val registry: MeterRegistry,
    private val stateRepository: DynamoDbStateRepository
) {
    private val logger = LoggerFactory.getLogger(KafkaStateConsumer::class.java)
    private var consumerJob: Job? = null
    private var eventConsumer: KafkaEventConsumer? = null
    private val stateMetrics = StateMetrics(registry)

    @PostConstruct
    fun start() {
        logger.info("Starting Kafka State Consumer...")

        val consumer = KafkaConsumerFactory(
            KafkaProperties(
                bootstrapServers = Environment.get(
                    "KAFKA_BOOTSTRAP_SERVERS",
                    "localhost:9092"
                ),
                clientId = "state-service",
                groupId = "state-service"
            )
        ).create()

        eventConsumer = KafkaEventConsumer(
            consumer = consumer,
            topics = listOf("home.device.events")
        ) { record ->
            stateMetrics.eventsReceived.increment()
            logger.debug("Received record: key={}, value={}", record.key(), record.value())
            // Note: In a real app we'd deserialize and call stateRepository.apply(event)
        }

        consumerJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                eventConsumer?.start()
            } catch (e: Exception) {
                logger.error("Error in Kafka consumer", e)
            }
        }
    }

    @PreDestroy
    fun stop() {
        logger.info("Stopping Kafka State Consumer...")
        eventConsumer?.stop()
        consumerJob?.cancel()
    }
}