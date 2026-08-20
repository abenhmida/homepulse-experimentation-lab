package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.state.failure.FailureType
import java.time.Duration
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class RetryPolicy(
    private val maxAttempts: Int,
    private val baseDelay: Duration = Duration.ofSeconds(1),
    private val maxDelay: Duration = Duration.ofMinutes(5)
) {

    fun decide(
        currentAttempt: Int,
        failureType: FailureType
    ): RetryDecision {

        if (failureType != FailureType.RETRYABLE) {
            return RetryDecision(
                shouldRetry = false,
                attempt = currentAttempt,
                delay = null,
                failureType = failureType
            )
        }

        if (currentAttempt >= maxAttempts) {
            return RetryDecision(
                shouldRetry = false,
                attempt = currentAttempt,
                delay = null,
                failureType = FailureType.PERMANENT
            )
        }

        val nextAttempt = currentAttempt + 1

        val exponential =
            baseDelay.multipliedBy(
                1L shl (nextAttempt - 1)
            )

        val delay =
            exponential.coerceAtMost(maxDelay)

        return RetryDecision(
            shouldRetry = true,
            attempt = nextAttempt,
            delay = delay,
            failureType = FailureType.RETRYABLE
        )
    }

    private fun jitter(
        delay: Duration,
        random: Random = Random.Default
    ): Duration {

        val factor =
            0.5 + random.nextDouble()

        return Duration.ofMillis(delay
            .toMillis()
            .times(factor)
            .toLong())
    }
}
