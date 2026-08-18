package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.state.failure.FailureType

data class RetryDecision(
    val retry: Boolean,
    val attempt: Int,
    val failureType: FailureType
)
