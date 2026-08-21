package com.krizaldis.homepulse.kafka.retry

import com.krizaldis.homepulse.event.retry.FailureType
import com.krizaldis.homepulse.event.retry.RetryMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant

class KafkaRetryHeaderMapperTest {

    @Test
    fun `should map retry metadata to kafka headers`() {
        val metadata = RetryMetadata(
            originalEventId = "evt-123",
            originalTopic = "home.events",
            originalPartition = 2,
            originalOffset = 100L,
            attempt = 3,
            firstFailedAt = Instant.parse("2026-08-21T10:00:00Z"),
            lastFailedAt = Instant.parse("2026-08-21T10:00:04Z"),
            nextAttemptAt = Instant.parse("2026-08-21T10:00:08Z"),
            failureType = FailureType.RETRYABLE,
            exceptionType = "java.lang.RuntimeException",
            correlationId = "corr-123",
            causationId = "cause-123"
        )

        val headers = KafkaRetryHeaderMapper.toHeaders(metadata)

        assertEquals("3", headers[KafkaRetryHeaders.ATTEMPT])
        assertEquals("evt-123", headers[KafkaRetryHeaders.ORIGINAL_EVENT_ID])
        assertEquals("home.events", headers[KafkaRetryHeaders.ORIGINAL_TOPIC])
        assertEquals("2", headers[KafkaRetryHeaders.ORIGINAL_PARTITION])
        assertEquals("100", headers[KafkaRetryHeaders.ORIGINAL_OFFSET])
        assertEquals("RETRYABLE", headers[KafkaRetryHeaders.FAILURE_TYPE])
        assertEquals("corr-123", headers[KafkaRetryHeaders.CORRELATION_ID])
        assertEquals("cause-123", headers[KafkaRetryHeaders.CAUSATION_ID])
    }

    @Test
    fun `should omit optional correlation and causation headers`() {
        val metadata = RetryMetadata(
            originalEventId = "evt-123",
            originalTopic = "home.events",
            originalPartition = 0,
            originalOffset = 1L,
            attempt = 1,
            firstFailedAt = Instant.parse("2026-08-21T10:00:00Z"),
            lastFailedAt = Instant.parse("2026-08-21T10:00:00Z"),
            nextAttemptAt = Instant.parse("2026-08-21T10:00:01Z"),
            failureType = FailureType.RETRYABLE,
            exceptionType = "java.lang.RuntimeException",
            correlationId = null,
            causationId = null
        )

        val headers = KafkaRetryHeaderMapper.toHeaders(metadata)

        assertNull(headers[KafkaRetryHeaders.CORRELATION_ID])
        assertNull(headers[KafkaRetryHeaders.CAUSATION_ID])
    }
}
