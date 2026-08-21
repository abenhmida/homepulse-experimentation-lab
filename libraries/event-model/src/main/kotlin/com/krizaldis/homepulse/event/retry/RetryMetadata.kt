package com.krizaldis.homepulse.event.retry

import java.time.Instant

data class RetryMetadata(
    val originalEventId: String,
    val originalTopic: String,
    val originalPartition: Int,
    val originalOffset: Long,

    val attempt: Int,

    val firstFailedAt: Instant,
    val lastFailedAt: Instant,
    val nextAttemptAt: Instant,

    val failureType: FailureType,
    val exceptionType: String,

    val correlationId: String?,
    val causationId: String?
)