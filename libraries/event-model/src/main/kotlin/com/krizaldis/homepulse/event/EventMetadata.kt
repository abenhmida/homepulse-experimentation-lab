package com.krizaldis.homepulse.event

import java.time.Instant

data class EventMetadata(
    val eventId: String,
    val eventType: String,
    val schemaVersion: Int,
    val homeId: String,
    val deviceId: String,
    val occurredAt: Instant,
    val sequenceNumber: Long,
    val correlationId: String? = null,
    val causationId: String? = null
) {
    init {
        require(eventId.isNotBlank()) {
            "eventId must not be blank"
        }

        require(eventType.isNotBlank()) {
            "eventType must not be blank"
        }

        require(schemaVersion > 0) {
            "schemaVersion must be greater than zero"
        }

        require(homeId.isNotBlank()) {
            "homeId must not be blank"
        }

        require(deviceId.isNotBlank()) {
            "deviceId must not be blank"
        }

        require(sequenceNumber >= 0) {
            "sequenceNumber must not be negative"
        }
    }
}