package com.krizaldis.homepulse.simulator

import com.krizaldis.homepulse.event.DeviceTemperatureReported
import com.krizaldis.homepulse.kafka.*
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Component
class DeviceSimulator(
    private val json: Json = Json { encodeDefaults = true }
) {
    private val logger = LoggerFactory.getLogger(DeviceSimulator::class.java)
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @PostConstruct
    fun start() {
        logger.info("Starting Device Simulator...")
        val properties = KafkaProperties(
            bootstrapServers = Environment.get("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
            clientId = "device-simulator"
        )

        val producer = KafkaProducerFactory(properties).create()
        val publisher = KafkaEventPublisher(producer)

        job = scope.launch {
            try {
                while (isActive) {
                    val event = DeviceTemperatureReported(
                        eventId = UUID.randomUUID().toString(),
                        sequenceNumber = Random.nextLong(),
                        homeId = "home-001",
                        deviceId = "thermostat-01",
                        temperature = Random.nextDouble(18.0, 25.0),
                        occurredAt = Instant.now().toString()
                    )
                    
                    val result = publisher.publish(
                        PublishedEvent(
                            topic = "home.device.events",
                            key = event.deviceId,
                            eventType = "DeviceTemperatureReported",
                            payload = json.encodeToString(event)
                        )
                    )

                    logger.info("Published eventId=${event.eventId} partition=${result.partition} offset=${result.offset}")
                    delay(2_000.milliseconds)
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    logger.error("Error in simulator loop", e)
                }
            } finally {
                producer.flush()
                producer.close()
            }
        }
    }

    @PreDestroy
    fun stop() {
        logger.info("Stopping Device Simulator...")
        job?.cancel()
    }
}
