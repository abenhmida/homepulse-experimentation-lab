package com.krizaldis.homepulse.state.infrastructure.kafka

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventMetadata
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.event.retry.FailureType
import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.event.retry.RetryMetadata
import com.krizaldis.homepulse.kafka.EventPublisher
import com.krizaldis.homepulse.kafka.PublishedEvent
import com.krizaldis.homepulse.kafka.PublishedRecord
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class KafkaRetryMessagePublisherTest {

    @Test
    fun `retry publication should use retry topic and preserve retry metadata`() = runBlocking {
        val publisher = RecordingEventPublisher()
        val sut = KafkaRetryMessagePublisher(publisher)
        val envelope = envelope(attempt = 2)

        val result = sut.publishRetry(envelope)

        assertEquals("home.events.retry.2", result.topic)
        assertEquals("home.events.retry.2", publisher.event?.topic)
        assertEquals("home-1:device-42", publisher.event?.key)
        assertEquals("home.device.temperature-reported", publisher.event?.eventType)
        assertNotNull(publisher.event?.payload)
        assertEquals("2", publisher.event?.headers?.get("homepulse-retry-attempt"))
        assertEquals("evt-42", publisher.event?.headers?.get("homepulse-original-event-id"))
        assertEquals("home.events", publisher.event?.headers?.get("homepulse-original-topic"))
        assertEquals("3", publisher.event?.headers?.get("homepulse-original-partition"))
        assertEquals("123", publisher.event?.headers?.get("homepulse-original-offset"))
        assertEquals("RETRYABLE", publisher.event?.headers?.get("homepulse-failure-type"))
    }

    @Test
    fun `dead letter publication should use dlq topic`() = runBlocking {
        val publisher = RecordingEventPublisher()
        val sut = KafkaRetryMessagePublisher(publisher)

        val result = sut.publishDeadLetter(envelope(attempt = 6))

        assertEquals("home.events.dlq", result.topic)
        assertEquals("home.events.dlq", publisher.event?.topic)
        assertEquals("6", publisher.event?.headers?.get("homepulse-retry-attempt"))
    }

    @Test
    fun `publisher should propagate correlation and causation headers`() = runBlocking {
        val publisher = RecordingEventPublisher()
        val sut = KafkaRetryMessagePublisher(publisher)

        sut.publishRetry(envelope(attempt = 1))

        assertEquals("corr-42", publisher.event?.headers?.get("correlation-id"))
        assertEquals("cause-41", publisher.event?.headers?.get("causation-id"))
    }

    @Test
    fun `publisher should propagate transport failure`() {
        val publisher = FailingEventPublisher()
        val sut = KafkaRetryMessagePublisher(publisher)

        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                sut.publishRetry(envelope(attempt = 1))
            }
        }
    }

    @Test
    fun `publisher should serialize the complete retry envelope`() = runBlocking {
        val publisher = RecordingEventPublisher()
        val sut = KafkaRetryMessagePublisher(publisher)

        sut.publishRetry(envelope(attempt = 1))

        val payload = requireNotNull(publisher.event?.payload)
        assert(payload.contains("\"retry\""))
        assert(payload.contains("\"event\""))
        assert(payload.contains("evt-42"))
        assert(payload.contains("temperatureCelsius"))
    }

    private fun envelope(attempt: Int): RetryEnvelope =
        RetryEnvelope(
            retry = RetryMetadata(
                originalEventId = "evt-42",
                originalTopic = "home.events",
                originalPartition = 3,
                originalOffset = 123L,
                attempt = attempt,
                firstFailedAt = Instant.parse("2026-08-21T10:00:00Z"),
                lastFailedAt = Instant.parse("2026-08-21T10:00:05Z"),
                nextAttemptAt = Instant.parse("2026-08-21T10:00:15Z"),
                failureType = FailureType.RETRYABLE,
                exceptionType = "java.lang.IllegalStateException",
                correlationId = "corr-42",
                causationId = "cause-41"
            ),
            event = DomainEvent(
                metadata = EventMetadata(
                    eventId = "evt-42",
                    eventType = "home.device.temperature-reported",
                    schemaVersion = 1,
                    homeId = "home-1",
                    deviceId = "device-42",
                    occurredAt = Instant.parse("2026-08-21T09:59:00Z"),
                    sequenceNumber = 42,
                    correlationId = "corr-42",
                    causationId = "cause-41"
                ),
                payload = TemperatureReported(
                    temperatureCelsius = 22.5,
                    humidityPercent = 45.0
                )
            )
        )

    private class FailingEventPublisher : EventPublisher {
        override fun publish(event: PublishedEvent): PublishedRecord {
            throw IllegalStateException("Kafka unavailable")
        }
    }

    private class RecordingEventPublisher : EventPublisher {
        var event: PublishedEvent? = null

        override fun publish(event: PublishedEvent): PublishedRecord {
            this.event = event
            return PublishedRecord(
                topic = event.topic,
                partition = 1,
                offset = 999L,
                timestamp = 123456789L
            )
        }
    }
}
