package com.krizaldis.homepulse.state.infrastructure.kafka

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventMetadata
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.event.retry.FailureType
import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.event.retry.RetryMetadata
import com.krizaldis.homepulse.serialization.JsonMapper
import com.krizaldis.homepulse.state.retry.RetryProcessingResult
import com.krizaldis.homepulse.state.retry.RetryProcessor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class RetryKafkaListenerTest {

    @Test
    fun `processed message is acknowledged`() {
        val processor = mockk<RetryProcessor>()
        val acknowledgment = mockk<org.springframework.kafka.support.Acknowledgment>(relaxed = true)
        val codec = mockk<EventJsonCodec>()
        val envelope = envelope()

        every { processor.process(any()) } returns RetryProcessingResult.Processed
        every { codec.deserializeRetryEnvelope(any()) } returns envelope

        RetryKafkaListener(processor, codec, ChaosFailureInjector()).consume(record(envelope), acknowledgment)

        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun `scheduled retry is acknowledged`() {
        val processor = mockk<RetryProcessor>()
        val acknowledgment = mockk<org.springframework.kafka.support.Acknowledgment>(relaxed = true)
        val codec = mockk<EventJsonCodec>()
        every { codec.deserializeRetryEnvelope(any()) } returns envelope()
        val publication = com.krizaldis.homepulse.state.retry.RetryPublicationResult("home.events.retry.2", 0, 1)

        every { processor.process(any()) } returns RetryProcessingResult.RetryScheduled(publication)

        RetryKafkaListener(processor, codec, ChaosFailureInjector()).consume(record(envelope()), acknowledgment)

        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun `dead lettered message is acknowledged`() {
        val processor = mockk<RetryProcessor>()
        val acknowledgment = mockk<org.springframework.kafka.support.Acknowledgment>(relaxed = true)
        val codec = mockk<EventJsonCodec>()
        every { codec.deserializeRetryEnvelope(any()) } returns envelope()
        val publication = com.krizaldis.homepulse.state.retry.RetryPublicationResult("home.events.dlq", 0, 2)

        every { processor.process(any()) } returns RetryProcessingResult.DeadLettered(publication)

        RetryKafkaListener(processor, codec, ChaosFailureInjector()).consume(record(envelope()), acknowledgment)

        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun `processing exception is propagated and message is not acknowledged`() {
        val processor = mockk<RetryProcessor>()
        val acknowledgment = mockk<org.springframework.kafka.support.Acknowledgment>(relaxed = true)
        val codec = mockk<EventJsonCodec>()

        every { processor.process(any()) } throws IllegalStateException("Kafka unavailable")
        every { codec.deserializeRetryEnvelope(any()) } returns envelope()

        assertThrows(IllegalStateException::class.java) {
            RetryKafkaListener(processor, codec, ChaosFailureInjector()).consume(record(envelope()), acknowledgment)
        }

        verify(exactly = 0) { acknowledgment.acknowledge() }
    }

    @Test
    fun `not ready message is not acknowledged`() {
        val processor = mockk<RetryProcessor>()
        val acknowledgment = mockk<org.springframework.kafka.support.Acknowledgment>(relaxed = true)
        val codec = mockk<EventJsonCodec>()
        val nextAttempt = Instant.parse("2026-08-21T10:01:00Z")

        every { processor.process(any()) } returns RetryProcessingResult.NotReady(nextAttempt)
        every { codec.deserializeRetryEnvelope(any()) } returns envelope()

        assertThrows(RetryNotReadyException::class.java) {
            RetryKafkaListener(processor, codec, ChaosFailureInjector()).consume(record(envelope()), acknowledgment)
        }

        verify(exactly = 0) { acknowledgment.acknowledge() }
    }

    private fun record(envelope: RetryEnvelope): ConsumerRecord<String, String> =
        ConsumerRecord(
            "home.events.retry.1",
            0,
            10L,
            "home-1:device-42",
            JsonMapper.mapper.writeValueAsString(envelope)
        )

    private fun envelope(): RetryEnvelope = RetryEnvelope(
        retry = RetryMetadata(
            originalEventId = "evt-42",
            originalTopic = "home.events",
            originalPartition = 0,
            originalOffset = 42,
            attempt = 1,
            firstFailedAt = Instant.parse("2026-08-21T09:59:00Z"),
            lastFailedAt = Instant.parse("2026-08-21T09:59:30Z"),
            nextAttemptAt = Instant.parse("2026-08-21T09:59:45Z"),
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
                occurredAt = Instant.parse("2026-08-21T09:58:00Z"),
                sequenceNumber = 42,
                correlationId = "corr-42",
                causationId = "cause-41"
            ),
            payload = TemperatureReported(22.5, 45.0)
        )
    )
}
