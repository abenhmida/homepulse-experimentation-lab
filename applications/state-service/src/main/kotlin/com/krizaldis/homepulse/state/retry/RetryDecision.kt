package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.state.failure.FailureType
import java.time.Duration

data class RetryDecision(
    val shouldRetry: Boolean,
    val attempt: Int,
    val delay: Duration?,
    val failureType: FailureType
)
