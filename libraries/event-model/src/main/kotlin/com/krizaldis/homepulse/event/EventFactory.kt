package com.krizaldis.homepulse.event
import java.time.Instant

object EventFactory {

    fun temperatureReported(
        eventId: String,
        homeId: String,
        deviceId: String,
        sequenceNumber: Long,
        occurredAt: Instant,
        temperatureCelsius: Double,
        humidityPercent: Double? = null,
        correlationId: String? = null,
        causationId: String? = null
    ): DomainEvent<TemperatureReported> {

        return DomainEvent(
            metadata = EventMetadata(
                eventId = eventId,
                eventType = EventType.TEMPERATURE_REPORTED.wireName,
                schemaVersion = EventVersion.V1.value,
                homeId = homeId,
                deviceId = deviceId,
                occurredAt = occurredAt,
                sequenceNumber = sequenceNumber,
                correlationId = correlationId,
                causationId = causationId
            ),
            payload = TemperatureReported(
                temperatureCelsius = temperatureCelsius,
                humidityPercent = humidityPercent
            )
        )
    }
}