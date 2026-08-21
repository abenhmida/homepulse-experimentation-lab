package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.state.failure.FailureType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class RetryMetadataFactoryTest {
    private val now = Instant.parse("2026-08-20T10:00:00Z")

    private val clock = Clock.fixed(now, ZoneId.of("UTC"))

    private val sut = RetryMetadataFactory(clock)

    @Test
    fun `should create metadata for first retry`() {

        val nextAttempt = Instant.parse("2026-08-20T10:00:02Z")

        val exception = IllegalStateException("temporary failure")

        val metadata = sut.create(
            originalEventId = "evt-001",
            originalTopic = "home.events",
            originalPartition = 2,
            originalOffset = 123L,
            attempt = 1,
            failureType = FailureType.RETRYABLE,
            exception = exception,
            correlationId = "corr-001",
            causationId = "cause-001",
            nextAttemptAt = nextAttempt
        )

        assertEquals("evt-001", metadata.originalEventId)
        assertEquals("home.events", metadata.originalTopic)
        assertEquals(2, metadata.originalPartition)
        assertEquals(123L, metadata.originalOffset)
        assertEquals(1, metadata.attempt)
        assertEquals(now, metadata.firstFailedAt)
        assertEquals(now, metadata.lastFailedAt)
        assertEquals(nextAttempt, metadata.nextAttemptAt)
        assertEquals(FailureType.RETRYABLE, metadata.failureType)
        assertEquals(IllegalStateException::class.qualifiedName, metadata.exceptionType)
        assertEquals("corr-001", metadata.correlationId)
        assertEquals("cause-001", metadata.causationId)
    }

    @Test
    fun `should preserve first failure timestamp`() {

        val firstFailure = RetryMetadata(
            originalEventId = "evt-001",
            originalTopic = "home.events",
            originalPartition = 0,
            originalOffset = 10L,
            attempt = 1,
            firstFailedAt = Instant.parse("2026-08-20T09:59:00Z"),
            lastFailedAt = Instant.parse("2026-08-20T09:59:00Z"),
            nextAttemptAt = Instant.parse("2026-08-20T09:59:02Z"),
            failureType = FailureType.RETRYABLE,
            exceptionType = "java.lang.RuntimeException",
            correlationId = "corr-001",
            causationId = null
        )

        val second = sut.create(
            originalEventId = firstFailure.originalEventId,
            originalTopic = firstFailure.originalTopic,
            originalPartition = firstFailure.originalPartition,
            originalOffset = firstFailure.originalOffset,
            attempt = 2,
            failureType = FailureType.RETRYABLE,
            exception = RuntimeException(),
            correlationId = firstFailure.correlationId,
            causationId = firstFailure.causationId,
            nextAttemptAt = Instant.parse("2026-08-20T10:00:04Z"),
            previous = firstFailure
        )

        assertEquals(firstFailure.firstFailedAt, second.firstFailedAt)
        assertEquals(2, second.attempt)
        assertEquals(now, second.lastFailedAt)
    }
}