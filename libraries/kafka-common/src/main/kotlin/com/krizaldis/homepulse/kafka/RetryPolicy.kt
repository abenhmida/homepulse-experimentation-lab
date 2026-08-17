package com.krizaldis.homepulse.kafka

import org.apache.kafka.common.protocol.types.Field

enum class FailureType {
    TRANSIENT,
    PERMANENT
}

data class RetryDecision(
    val retry: Boolean,
    val attempt: Int,
    val failureType: FailureType
)

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
            failureType = FailureType.TRANSIENT
        )
    }
}