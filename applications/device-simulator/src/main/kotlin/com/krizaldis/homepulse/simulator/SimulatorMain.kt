package com.krizaldis.homepulse.simulator

import com.krizaldis.homepulse.event.DeviceTemperatureReported
import com.krizaldis.homepulse.kafka.Environment
import com.krizaldis.homepulse.kafka.KafkaEventPublisher
import com.krizaldis.homepulse.kafka.KafkaProducerFactory
import com.krizaldis.homepulse.kafka.KafkaProperties
import com.krizaldis.homepulse.kafka.PublishedEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

fun main() = runBlocking {
    val properties = KafkaProperties(
        bootstrapServers = Environment.get(
            "KAFKA_BOOTSTRAP_SERVERS",
            "localhost:9092"
        ),
        clientId = "device-simulator"
    )

    val producer = KafkaProducerFactory(properties).create()
    val publisher = KafkaEventPublisher(producer)

    val json = Json{
        encodeDefaults = true
    }

    try {
        while (true) {
            val event = DeviceTemperatureReported(
                eventId = UUID.randomUUID().toString(),
                sequenceNumber = Random.nextLong(),
                homeId = "home-001",
                deviceId = "thermostat-01",
                temperature = Random.nextDouble(
                    18.0,
                    25.0
                ),
                occurredAt = Instant.now().toString()
            )
            val result = publisher.publish(PublishedEvent(
                topic = "home.device.events",
                key = event.deviceId,
                eventType = "DeviceTemperatureReported",
                payload = json.encodeToString(event)
            ))

            println(
                "Published " +
                        "eventId=${event.eventId} " +
                        "partition=${result.partition} " +
                        "offset=${result.offset}"
            )

            delay(2_000.milliseconds)
        }
    } finally {
        producer.flush()
        producer.close()
    }
}