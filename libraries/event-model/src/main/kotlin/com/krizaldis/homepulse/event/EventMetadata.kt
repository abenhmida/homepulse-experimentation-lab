package com.krizaldis.homepulse.event

import java.time.Instant

data class EventMetadata(
    val eventId: String,
    val eventType: String,
    val schemaVersion: Int,
    val deviceId: String,
    val occurredAt: Instant,
    val sequenceNumber: Long,
    val correlationId: String? = null,
)