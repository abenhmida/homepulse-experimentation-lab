package com.krizaldis.homepulse.state.retry

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class RetryEnvelope(
    val originalTopic: String,
    val originalPartition: Int,
    val originalOffset: Long,
    val attempt: Int,
    @Contextual
    val firstFailureAt: Instant,
    @Contextual
    val lastFailureAt: Instant,

    val failureType: String,
    val failureMessage: String?
)
