package com.krizaldis.homepulse.events

import java.time.Instant

data class EventMetadata(
    val eventId: String,
    val eventType: EventType,
    val eventVersion: Int,
    val occurredAt: Instant,
    val producedAt: Instant,
    val homeId: String,
    val deviceId: String,
    val correlationId: String,
    val causationId: String?,
    val source: String
)