package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.state.failure.FailureType
import java.time.Instant

data class RetryMetadata(
    val eventId: String,
    val originalTopic: String,
    val originalPartition: Int,
    val originalOffset: Long,
    val attempt: Int,
    val firstFailureAt: Instant,
    val lastFailureAt: Instant,
    val failureType: FailureType,
    val failureClass: String,
    val failureMessage: String?,
    val nextAttemptAt: Instant
)