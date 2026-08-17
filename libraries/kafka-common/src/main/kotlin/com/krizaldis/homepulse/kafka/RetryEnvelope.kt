package com.krizaldis.homepulse.kafka

import kotlinx.serialization.Serializable

@Serializable
data class RetryEnvelope(
    val originalTopic: String,
    val originalPartition: Int,
    val originalOffset: Long,
    val eventId: String,
    val attempt: Int,
    val failedAt: String,
    val reason: String,
    val payload: String
)
