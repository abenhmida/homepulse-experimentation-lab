package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.state.failure.FailureType

class RetryPolicy(
    private val maxAttempts: Int
) {
    fun decide(
        currentAttempt: Int,
        failureType: FailureType
    ): RetryDecision {
        if (failureType == FailureType.PERMANENT) {
            return RetryDecision(
                retry = false,
                attempt = currentAttempt,
                failureType = FailureType.PERMANENT
            )
        }

        if (currentAttempt >= maxAttempts) {
            return RetryDecision(
                retry = false,
                attempt = currentAttempt,
                failureType = FailureType.PERMANENT
            )
        }

        return RetryDecision(
            retry = true,
            attempt = currentAttempt + 1,
            failureType = FailureType.RETRYABLE
        )
    }
}
