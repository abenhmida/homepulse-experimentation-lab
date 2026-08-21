package com.krizaldis.homepulse.state.retry

import java.time.Duration

/**
 * Application-level decision about what should happen after a retryable failure.
 *
 * Topic names, Kafka headers and ProducerRecord objects deliberately do not
 * appear here. Those are infrastructure concerns.
 */
sealed interface RetryDecision {

    data class Retry(
        val attempt: Int,
        val delay: Duration
    ) : RetryDecision

    data class DeadLetter(
        val attempt: Int
    ) : RetryDecision
}
