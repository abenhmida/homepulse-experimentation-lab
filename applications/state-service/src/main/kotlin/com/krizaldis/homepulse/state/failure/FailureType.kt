package com.krizaldis.homepulse.state.failure

enum class FailureType {
    NONE,
    DUPLICATE,
    STALE,
    RETRYABLE,
    PERMANENT
}