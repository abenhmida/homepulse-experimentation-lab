package com.krizaldis.homepulse.state.dlq

import com.krizaldis.homepulse.state.failure.FailureType
import java.time.Instant

data class DlqRecord(
    val eventId: String,
    val originalTopic: String,
    val originalPartition: Int,
    val originalOffset: Long,
    val attempt: Int,
    val failureType: FailureType,
    val failureClass: String,
    val failureMessage: String?,
    val firstFailureAt: Instant,
    val lastFailureAt: Instant,
    val dlqAt: Instant
)
