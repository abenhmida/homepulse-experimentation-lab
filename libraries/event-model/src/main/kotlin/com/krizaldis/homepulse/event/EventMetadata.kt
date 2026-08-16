package com.krizaldis.homepulse.event

import java.time.Instant

data class EventMetadata(
    val eventId: String,
    val eventType: EventType,
    val eventVersion: EventVersion,
    val occurredAt: Instant,
    val producedAt: Instant,
    val homeId: String,
    val deviceId: String,
    val correlationId: String,
    val causationId: String?,
    val source: String
)