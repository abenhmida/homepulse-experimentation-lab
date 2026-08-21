package com.krizaldis.homepulse.state.retry

import java.time.Instant

sealed interface RetryProcessingResult {
    data object Processed : RetryProcessingResult

    data class RetryScheduled(
        val publication: RetryPublicationResult
    ) : RetryProcessingResult

    data class DeadLettered(
        val publication: RetryPublicationResult
    ) : RetryProcessingResult

    data class NotReady(
        val nextAttemptAt: Instant
    ) : RetryProcessingResult
}