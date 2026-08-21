package com.krizaldis.homepulse.state.application

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventMetadata
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.event.retry.FailureType
import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.event.retry.RetryMetadata
import com.krizaldis.homepulse.state.retry.RetryEnvelopeFactory
import com.krizaldis.homepulse.state.retry.RetryMessagePublisher
import com.krizaldis.homepulse.state.retry.RetryPolicy
import com.krizaldis.homepulse.state.retry.RetryPolicyConfig
import com.krizaldis.homepulse.state.retry.RetryProcessingResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class RetryProcessingServiceTest {

    private val now = Instant.parse("2026-08-21T10:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private lateinit var stateService: StateService
    private lateinit var publisher: RetryMessagePublisher
    private lateinit var sut: RetryProcessingService

    private val retryableException = RuntimeException("DynamoDB timeout")

    @BeforeEach
    fun setUp() {
        stateService = mockk()
        publisher = mockk()

        val policy = RetryPolicy(
            RetryPolicyConfig(
                maxAttempts = 3,
                initialDelay = Duration.ofSeconds(5),
                maxDelay = Duration.ofMinutes(1),
                multiplier = 2.0,
                jitterFactor = 0.0
            )
        )

        sut = RetryProcessingService(
            stateService = stateService,
            retryPolicy = policy,
            retryEnvelopeFactory = RetryEnvelopeFactory(),
            publisher = publisher,
            clock = clock
        )
    }

    @Test
    fun `successful processing returns Processed`() {
        every { stateService.process(any()) } returns com.krizaldis.homepulse.state.domain.ProcessingResult.Applied

        val result = sut.process(envelope(attempt = 1))

        assertEquals(RetryProcessingResult.Processed, result)
        verify(exactly = 0) { publisher.publishRetry(any()) }
        verify(exactly = 0) { publisher.publishDeadLetter(any()) }
    }

    @Test
    fun `duplicate is terminal success`() {
        every { stateService.process(any()) } returns com.krizaldis.homepulse.state.domain.ProcessingResult.Duplicate

        assertEquals(
            RetryProcessingResult.Processed,
            sut.process(envelope(attempt = 2))
        )
    }

    @Test
    fun `retryable failure publishes next attempt with calculated delay`() {
        every { stateService.process(any()) } returns
            com.krizaldis.homepulse.state.domain.ProcessingResult.RetryableFailure(retryableException)

        val publication = com.krizaldis.homepulse.state.retry.RetryPublicationResult(
            "home.events.retry.2", 0, 10
        )
        every { publisher.publishRetry(any()) } returns publication

        val result = sut.process(envelope(attempt = 1))

        val scheduled = assertInstanceOf(
            RetryProcessingResult.RetryScheduled::class.java,
            result
        )
        assertEquals(publication, scheduled.publication)

        verify {
            publisher.publishRetry(match {
                it.retry.attempt == 2 &&
                    it.retry.nextAttemptAt == now.plusSeconds(5) &&
                    it.retry.failureType == FailureType.RETRYABLE &&
                    it.retry.exceptionType == retryableException.javaClass.name
            })
        }
    }

    @Test
    fun `permanent failure goes directly to DLQ`() {
        val exception = IllegalArgumentException("bad event")
        every { stateService.process(any()) } returns
            com.krizaldis.homepulse.state.domain.ProcessingResult.PermanentFailure(exception)

        val publication = com.krizaldis.homepulse.state.retry.RetryPublicationResult(
            "home.events.dlq", 0, 11
        )
        every { publisher.publishDeadLetter(any()) } returns publication

        val result = sut.process(envelope(attempt = 1))

        val deadLettered = assertInstanceOf(
            RetryProcessingResult.DeadLettered::class.java,
            result
        )
        assertEquals(publication, deadLettered.publication)

        verify(exactly = 0) { publisher.publishRetry(any()) }
        verify {
            publisher.publishDeadLetter(match {
                it.retry.failureType == FailureType.PERMANENT &&
                    it.retry.attempt == 1
            })
        }
    }

    @Test
    fun `retry exhaustion goes to DLQ`() {
        every { stateService.process(any()) } returns
            com.krizaldis.homepulse.state.domain.ProcessingResult.RetryableFailure(retryableException)

        val publication = com.krizaldis.homepulse.state.retry.RetryPublicationResult(
            "home.events.dlq", 0, 12
        )
        every { publisher.publishDeadLetter(any()) } returns publication

        val result = sut.process(envelope(attempt = 3))

        assertInstanceOf(RetryProcessingResult.DeadLettered::class.java, result)
        verify(exactly = 1) { publisher.publishDeadLetter(any()) }
        verify(exactly = 0) { publisher.publishRetry(any()) }
    }

    @Test
    fun `publisher failure is propagated and therefore cannot be acknowledged`() {
        every { stateService.process(any()) } returns
            com.krizaldis.homepulse.state.domain.ProcessingResult.RetryableFailure(retryableException)
        every { publisher.publishRetry(any()) } throws IllegalStateException("Kafka down")

        assertThrows(IllegalStateException::class.java) {
            sut.process(envelope(attempt = 1))
        }
    }

    @Test
    fun `event is not processed before next attempt time`() {
        val futureEnvelope = envelope(
            attempt = 2,
            nextAttemptAt = now.plusSeconds(30)
        )

        val result = sut.process(futureEnvelope)

        assertEquals(
            RetryProcessingResult.NotReady(now.plusSeconds(30)),
            result
        )
        verify(exactly = 0) { stateService.process(any()) }
    }

    private fun envelope(
        attempt: Int,
        nextAttemptAt: Instant = now.minusSeconds(1)
    ): RetryEnvelope = RetryEnvelope(
        retry = RetryMetadata(
            originalEventId = "evt-42",
            originalTopic = "home.events",
            originalPartition = 2,
            originalOffset = 42,
            attempt = attempt,
            firstFailedAt = now.minus(5, ChronoUnit.MINUTES),
            lastFailedAt = now.minusSeconds(30),
            nextAttemptAt = nextAttemptAt,
            failureType = FailureType.RETRYABLE,
            exceptionType = "java.lang.RuntimeException",
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
                occurredAt =  now.minus(10, ChronoUnit.MINUTES),
                sequenceNumber = 42,
                correlationId = "corr-42",
                causationId = "cause-41"
            ),
            payload = TemperatureReported(22.5, 45.0)
        )
    )
}
