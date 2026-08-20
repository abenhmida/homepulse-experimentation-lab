package com.krizaldis.homepulse.simulator

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventMetadata
import com.krizaldis.homepulse.event.EventTypes
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.kafka.JsonEventSerializer
import com.krizaldis.homepulse.observability.KafkaPropagation
import com.krizaldis.homepulse.observability.Telemetry
import com.krizaldis.homepulse.observability.Tracing
import io.opentelemetry.context.Context
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID

class EventProducer(
    private val config: ProducerConfig,
    private val telemetry: Telemetry
) : AutoCloseable {

    private val log = LoggerFactory.getLogger(EventProducer::class.java)

    private val producer = KafkaProducer<String, ByteArray>(
        mapOf(
            "bootstrap.servers" to config.bootstrapServers,
            "client.id" to config.clientId,
            "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
            "value.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer",
            "acks" to "all",
            "enable.idempotence" to "true",
            "retries" to Int.MAX_VALUE,
            "delivery.timeout.ms" to 120_000,
            "request.timeout.ms" to 30_000,
            "linger.ms" to 5
        )
    )

    fun run() {
        repeat(config.events) { index ->
            publish(index)
            Thread.sleep(config.intervalMs)
        }

        producer.flush()
    }

    private fun publish(index: Int) {
        val deviceId = "thermostat-%02d".format((index % 10) + 1)
        val event = DomainEvent(
            metadata = EventMetadata(
                eventId = UUID.randomUUID().toString(),
                eventType = EventTypes.TEMPERATURE_READING,
                schemaVersion = 1,
                deviceId = deviceId,
                occurredAt = Instant.now(),
                sequenceNumber = index.toLong() + 1,
                correlationId = "lab-${UUID.randomUUID()}",
                homeId = "Home1",
                causationId = UUID.randomUUID().toString(),
            ),
            payload = TemperatureReported(
                temperatureCelsius = 19.0 + (index % 10),
                humidityPercent = 40.0 + (index % 20)
            )
        )

        val record = ProducerRecord(
            config.topic,
            deviceId,
            JsonEventSerializer().serialize(event)
        )

        val span = Tracing.producerSpan(telemetry, config.topic)

        try {
            span.makeCurrent().use {
                span.setAttribute("event.id", event.metadata.eventId)
                span.setAttribute("event.type", event.metadata.eventType)
                span.setAttribute("device.id", event.metadata.deviceId)
                KafkaPropagation.inject(context = Context.current(), record = record)

                producer.send(record){ metadata, exception ->
                    if (exception != null) {
                        Tracing.recordFailure(span, exception)
                        log.error("Kafka publish failed eventId={}", event.metadata.eventId, exception)
                    } else {
                        log.info(
                            "Published eventId={} topic={} partition={} offset={}",
                            event.metadata.eventId,
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset()
                        )
                        span.end()
                    }
                }
            }
        }catch (ex: Exception){
            Tracing.recordFailure(span, ex)
            span.end()
            throw ex
        }
    }

    override fun close() {
        producer.close()
    }

}