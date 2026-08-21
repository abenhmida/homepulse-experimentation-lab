package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.event.retry.FailureType
import com.krizaldis.homepulse.event.retry.RetryMetadata
import java.time.Clock
import java.time.Instant

class RetryMetadataFactory(
    private val clock: Clock = Clock.systemUTC(),
) {
    fun create(
        originalEventId: String,
        originalTopic: String,
        originalPartition: Int,
        originalOffset: Long,
        attempt: Int,
        failureType: FailureType,
        exception: Throwable,
        correlationId: String?,
        causationId: String?,
        nextAttemptAt: Instant,
        previous: RetryMetadata? = null
    ): RetryMetadata {
        val now = Instant.now(clock)

        return RetryMetadata(
            originalEventId = originalEventId,
            originalTopic = originalTopic,
            originalPartition = originalPartition,
            originalOffset = originalOffset,
            attempt = attempt,
            firstFailedAt = previous?.firstFailedAt ?: now,
            lastFailedAt = now,
            nextAttemptAt = nextAttemptAt,
            failureType = failureType,
            exceptionType =
                exception::class.qualifiedName
                    ?: exception::class.simpleName
                    ?: "UnknownException",
            correlationId = correlationId,
            causationId = causationId
        )
    }
}
