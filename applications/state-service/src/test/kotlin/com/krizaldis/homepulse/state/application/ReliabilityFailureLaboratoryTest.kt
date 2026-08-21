package com.krizaldis.homepulse.state.application

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventMetadata
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.event.retry.FailureType
import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.event.retry.RetryMetadata
import com.krizaldis.homepulse.state.domain.ProcessingResult
import com.krizaldis.homepulse.state.retry.RetryEnvelopeFactory
import com.krizaldis.homepulse.state.retry.RetryMessagePublisher
import com.krizaldis.homepulse.state.retry.RetryPolicy
import com.krizaldis.homepulse.state.retry.RetryPolicyConfig
import com.krizaldis.homepulse.state.retry.RetryProcessingResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit


class ReliabilityFailureLaboratoryTest {

    private val now = Instant.parse("2026-08-21T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `retry publication failure means no successful processing outcome`() {
        val stateService = mockk<StateService>()
        val publisher = mockk<RetryMessagePublisher>()
        every { stateService.process(any()) } returns
            ProcessingResult.RetryableFailure(IllegalStateException("DynamoDB unavailable"))
        every { publisher.publishRetry(any()) } throws IllegalStateException("Kafka unavailable")

        val sut = service(stateService, publisher)

        assertThrows(IllegalStateException::class.java) {
            sut.process(envelope(1))
        }
    }

    @Test
    fun `dlq publication failure means no successful processing outcome`() {
        val stateService = mockk<StateService>()
        val publisher = mockk<RetryMessagePublisher>()
        every { stateService.process(any()) } returns
            ProcessingResult.PermanentFailure(IllegalArgumentException("poison message"))
        every { publisher.publishDeadLetter(any()) } throws IllegalStateException("Kafka unavailable")

        val sut = service(stateService, publisher)

        assertThrows(IllegalStateException::class.java) {
            sut.process(envelope(1))
        }
    }

    @Test
    fun `crash window after publication is safe because retry result is durable before acknowledgement`() {
        val stateService = mockk<StateService>()
        val publisher = mockk<RetryMessagePublisher>()
        val publication = com.krizaldis.homepulse.state.retry.RetryPublicationResult(
            "home.events.retry.2", 0, 100
        )

        every { stateService.process(any()) } returns
            ProcessingResult.RetryableFailure(IllegalStateException("temporary failure"))
        every { publisher.publishRetry(any()) } returns publication

        val result = service(stateService, publisher).process(envelope(1))

        assertEquals(
            RetryProcessingResult.RetryScheduled(publication),
            result
        )

        // The Kafka adapter owns acknowledgement. The application result says
        // only that the replacement record was successfully published.
        verify(exactly = 1) { publisher.publishRetry(any()) }
    }

    @Test
    fun `permanent poison message bypasses retry`() {
        val stateService = mockk<StateService>()
        val publisher = mockk<RetryMessagePublisher>()
        val publication = com.krizaldis.homepulse.state.retry.RetryPublicationResult(
            "home.events.dlq", 0, 101
        )

        every { stateService.process(any()) } returns
            ProcessingResult.PermanentFailure(IllegalArgumentException("invalid payload"))
        every { publisher.publishDeadLetter(any()) } returns publication

        val result = service(stateService, publisher).process(envelope(1))

        assertEquals(
            RetryProcessingResult.DeadLettered(publication),
            result
        )
        verify(exactly = 0) { publisher.publishRetry(any()) }
        verify(exactly = 1) { publisher.publishDeadLetter(any()) }
    }

    @Test
    fun `retry exhaustion transitions to dlq`() {
        val stateService = mockk<StateService>()
        val publisher = mockk<RetryMessagePublisher>()
        val publication = com.krizaldis.homepulse.state.retry.RetryPublicationResult(
            "home.events.dlq", 0, 102
        )

        every { stateService.process(any()) } returns
            ProcessingResult.RetryableFailure(IllegalStateException("still failing"))
        every { publisher.publishDeadLetter(any()) } returns publication

        val result = service(stateService, publisher).process(envelope(3))

        assertEquals(RetryProcessingResult.DeadLettered(publication), result)
        verify(exactly = 0) { publisher.publishRetry(any()) }
    }

    @Test
    fun `retry arriving too early is not processed`() {
        val stateService = mockk<StateService>()
        val publisher = mockk<RetryMessagePublisher>()
        val sut = service(stateService, publisher)

        val result = sut.process(envelope(2, now.plusSeconds(60)))

        assertEquals(
            RetryProcessingResult.NotReady(now.plusSeconds(60)),
            result
        )
        verify(exactly = 0) { stateService.process(any()) }
        verify(exactly = 0) { publisher.publishRetry(any()) }
        verify(exactly = 0) { publisher.publishDeadLetter(any()) }
    }

    private fun service(
        stateService: StateService,
        publisher: RetryMessagePublisher
    ) = RetryProcessingService(
        stateService = stateService,
        retryPolicy = RetryPolicy(
            RetryPolicyConfig(
                maxAttempts = 3,
                initialDelay = Duration.ofSeconds(5),
                maxDelay = Duration.ofMinutes(1),
                multiplier = 2.0,
                jitterFactor = 0.0
            )
        ),
        retryEnvelopeFactory = RetryEnvelopeFactory(),
        publisher = publisher,
        clock = clock
    )

    private fun envelope(
        attempt: Int,
        nextAttemptAt: Instant = now.minusSeconds(1)
    ) = RetryEnvelope(
        retry = RetryMetadata(
            originalEventId = "evt-42",
            originalTopic = "home.events",
            originalPartition = 0,
            originalOffset = 42,
            attempt = attempt,
            firstFailedAt = now.minus(5, ChronoUnit.MINUTES),
            lastFailedAt = now.minus(1, ChronoUnit.MINUTES),
            nextAttemptAt = nextAttemptAt,
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
                occurredAt = now.minus(10, ChronoUnit.MINUTES),
                sequenceNumber = 42,
                correlationId = "corr-42",
                causationId = "cause-41"
            ),
            payload = TemperatureReported(22.5, 45.0)
        )
    )
}
