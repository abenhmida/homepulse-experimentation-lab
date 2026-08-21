package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.event.retry.FailureType
import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.event.retry.RetryMetadata
import java.time.Instant

class RetryEnvelopeFactory {

    fun initialFailure(
        event: com.krizaldis.homepulse.event.DomainEvent<*>,
        originalTopic: String,
        originalPartition: Int,
        originalOffset: Long,
        failure: FailureType,
        exception: Throwable,
        attempt: Int,
        nextAttemptAt: Instant,
        now: Instant
    ): RetryEnvelope = RetryEnvelope(
        retry = RetryMetadata(
            originalEventId = event.metadata.eventId,
            originalTopic = originalTopic,
            originalPartition = originalPartition,
            originalOffset = originalOffset,
            attempt = attempt,
            firstFailedAt = now,
            lastFailedAt = now,
            nextAttemptAt = nextAttemptAt,
            failureType = failure,
            exceptionType = exception.javaClass.name,
            correlationId = event.metadata.correlationId,
            causationId = event.metadata.causationId
        ),
        event = event
    )

    fun nextAttempt(
        current: RetryEnvelope,
        failure: FailureType,
        exception: Throwable,
        nextAttempt: Int,
        nextAttemptAt: Instant,
        now: Instant
    ): RetryEnvelope = RetryEnvelope(
        retry = RetryMetadata(
            originalEventId = current.retry.originalEventId,
            originalTopic = current.retry.originalTopic,
            originalPartition = current.retry.originalPartition,
            originalOffset = current.retry.originalOffset,
            attempt = nextAttempt,
            firstFailedAt = current.retry.firstFailedAt,
            lastFailedAt = now,
            nextAttemptAt = nextAttemptAt,
            failureType = failure,
            exceptionType = exception.javaClass.name,
            correlationId = current.retry.correlationId,
            causationId = current.retry.causationId
        ),
        event = current.event
    )

    fun deadLetter(
        current: RetryEnvelope,
        failure: FailureType,
        exception: Throwable,
        now: Instant
    ): RetryEnvelope = RetryEnvelope(
        retry = RetryMetadata(
            originalEventId = current.retry.originalEventId,
            originalTopic = current.retry.originalTopic,
            originalPartition = current.retry.originalPartition,
            originalOffset = current.retry.originalOffset,
            attempt = current.retry.attempt,
            firstFailedAt = current.retry.firstFailedAt,
            lastFailedAt = now,
            nextAttemptAt = now,
            failureType = failure,
            exceptionType = exception.javaClass.name,
            correlationId = current.retry.correlationId,
            causationId = current.retry.causationId
        ),
        event = current.event
    )
}
