package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.state.failure.FailureType
import java.time.Duration


sealed interface RetryDecision {
    data class Retry(
        val attempt: Int,
        val delay: Duration
    ) : RetryDecision

    data class DeadLetter(
        val attempt: Int
    ) : RetryDecision
}