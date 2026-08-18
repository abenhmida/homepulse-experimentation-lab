package com.krizaldis.homepulse.state.retry

import java.time.Instant

data class RetryMetadata(
    val originalTopic: String,
    val originalPartition: Int,
    val originalOffset: Long,
    val attempt: Int,
    val firstFailureAt: Instant,
    val lastFailureAt: Instant,
    val failureType: String,
    val failureMessage: String?,
    val nextAttemptAt: Instant,
)