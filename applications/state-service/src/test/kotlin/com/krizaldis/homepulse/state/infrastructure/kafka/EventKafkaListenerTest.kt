package com.krizaldis.homepulse.state.infrastructure.kafka

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventMetadata
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.state.application.StateService
import com.krizaldis.homepulse.state.domain.ProcessingResult
import com.krizaldis.homepulse.state.retry.RetryEnvelopeFactory
import com.krizaldis.homepulse.state.retry.RetryMessagePublisher
import com.krizaldis.homepulse.state.retry.RetryPolicy
import com.krizaldis.homepulse.state.retry.RetryPolicyConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.Acknowledgment
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class EventKafkaListenerTest {

    private val now = Instant.parse("2026-08-21T10:00:00Z")

    @Test
    fun `successful event is acknowledged`() {
        val stateService = mockk<StateService>()
        val publisher = mockk<RetryMessagePublisher>()
        val codec = mockk<EventJsonCodec>()
        val acknowledgment = mockk<Acknowledgment>(relaxed = true)
        val event = event()

        every { codec.deserializeEvent(any()) } returns event
        every { stateService.process(event) } returns ProcessingResult.Applied

        listener(stateService, publisher, codec).consume(record(), acknowledgment)

        verify(exactly = 1) { acknowledgment.acknowledge() }
        verify(exactly = 0) { publisher.publishRetry(any()) }
        verify(exactly = 0) { publisher.publishDeadLetter(any()) }
    }

    @Test
    fun `retryable initial failure publishes first retry and then acknowledges`() {
        val stateService = mockk<StateService>()
        val publisher = mockk<RetryMessagePublisher>()
        val codec = mockk<EventJsonCodec>()
        val acknowledgment = mockk<Acknowledgment>(relaxed = true)
        val event = event()
        val exception = IllegalStateException("DynamoDB unavailable")

        every { codec.deserializeEvent(any()) } returns event
        every { stateService.process(event) } returns ProcessingResult.RetryableFailure(exception)
        every { publisher.publishRetry(any()) } returns
            com.krizaldis.homepulse.state.retry.RetryPublicationResult("home.events.retry.1", 0, 1)

        listener(stateService, publisher, codec).consume(record(), acknowledgment)

        verify {
            publisher.publishRetry(match {
                it.retry.attempt == 1 &&
                    it.retry.originalTopic == "home.events" &&
                    it.retry.originalPartition == 2 &&
                    it.retry.originalOffset.toInt() == 42 &&
                    it.retry.nextAttemptAt == now.plusSeconds(1)
            })
        }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun `permanent initial failure publishes DLQ and then acknowledges`() {
        val stateService = mockk<StateService>()
        val publisher = mockk<RetryMessagePublisher>()
        val codec = mockk<EventJsonCodec>()
        val acknowledgment = mockk<Acknowledgment>(relaxed = true)
        val event = event()
        val exception = IllegalArgumentException("poison")

        every { codec.deserializeEvent(any()) } returns event
        every { stateService.process(event) } returns ProcessingResult.PermanentFailure(exception)
        every { publisher.publishDeadLetter(any()) } returns
            com.krizaldis.homepulse.state.retry.RetryPublicationResult("home.events.dlq", 0, 2)

        listener(stateService, publisher, codec).consume(record(), acknowledgment)

        verify(exactly = 1) { publisher.publishDeadLetter(any()) }
        verify(exactly = 0) { publisher.publishRetry(any()) }
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun `retry publication failure prevents acknowledgement`() {
        val stateService = mockk<StateService>()
        val publisher = mockk<RetryMessagePublisher>()
        val codec = mockk<EventJsonCodec>()
        val acknowledgment = mockk<Acknowledgment>(relaxed = true)
        val event = event()

        every { codec.deserializeEvent(any()) } returns event
        every { stateService.process(event) } returns
            ProcessingResult.RetryableFailure(IllegalStateException("temporary"))
        every { publisher.publishRetry(any()) } throws IllegalStateException("Kafka unavailable")

        assertThrows(IllegalStateException::class.java) {
            listener(stateService, publisher, codec).consume(record(), acknowledgment)
        }

        verify(exactly = 0) { acknowledgment.acknowledge() }
    }

    private fun listener(
        stateService: StateService,
        publisher: RetryMessagePublisher,
        codec: EventJsonCodec
    ) = EventKafkaListener(
        stateService = stateService,
        retryPolicy = RetryPolicy(
            RetryPolicyConfig(
                maxAttempts = 3,
                initialDelay = Duration.ofSeconds(1),
                maxDelay = Duration.ofMinutes(1),
                multiplier = 2.0,
                jitterFactor = 0.0
            )
        ),
        retryEnvelopeFactory = RetryEnvelopeFactory(),
        retryPublisher = publisher,
        codec = codec,
        clock = Clock.fixed(now, ZoneOffset.UTC),
        chaosFailureInjector = ChaosFailureInjector()
    )

    private fun record() = ConsumerRecord<String, String>(
        "home.events",
        2,
        42L,
        "home-1:device-42",
        "{}"
    )

    private fun event() = DomainEvent(
        metadata = EventMetadata(
            eventId = "evt-42",
            eventType = "home.device.temperature-reported",
            schemaVersion = 1,
            homeId = "home-1",
            deviceId = "device-42",
            occurredAt = now.minus(1, ChronoUnit.MINUTES),
            sequenceNumber = 42
        ),
        payload = TemperatureReported(22.0, 45.0)
    )
}
