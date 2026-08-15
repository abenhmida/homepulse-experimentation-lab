package com.krizaldis.homepulse.events

import java.time.Clock
import java.time.Instant
import java.util.UUID

class EventFactory(
    private val clock: Clock = Clock.systemUTC()
) {
    fun temperatureMeasured(
        homeId: String,
        deviceId: String,
        temperature: Double,
        unit: TemperatureUnit,
        correlationId: String = UUID.randomUUID().toString(),
        causationId: String? = null
    ): DomainEvent<TemperatureMeasured> {

        val now: Instant = clock.instant()

        val metadata = EventMetadata(
            eventId = UUID.randomUUID().toString(),
            eventType = EventType.TEMPERATURE_MEASURED,
            eventVersion = 1,
            occurredAt = now,
            producedAt = now,
            homeId = homeId,
            deviceId = deviceId,
            correlationId = correlationId,
            causationId = causationId,
            source = deviceId
        )

        return DomainEvent(
            metadata = metadata,
            payload = TemperatureMeasured(
                temperature = temperature,
                unit = unit
            )
        )
    }
}