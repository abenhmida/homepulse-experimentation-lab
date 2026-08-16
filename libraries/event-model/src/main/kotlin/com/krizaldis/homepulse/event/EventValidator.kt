package com.krizaldis.homepulse.event

class EventValidator {
    fun validate(event: DomainEvent<*>) {
        require(event.metadata.eventId.isNotBlank()) {
            "eventId must not be blank"
        }

        require(event.metadata.homeId.isNotBlank()) {
            "homeId must not be blank"
        }

        require(event.metadata.deviceId.isNotBlank()) {
            "deviceId must not be blank"
        }

        require(event.metadata.source.isNotBlank()) {
            "source must not be blank"
        }

        require(event.metadata.correlationId.isNotBlank()) {
            "correlationId must not be blank"
        }

        require(
            !event.metadata.producedAt.isBefore(event.metadata.occurredAt)
        ) {
            "producedAt cannot be before occurredAt"
        }
    }
}