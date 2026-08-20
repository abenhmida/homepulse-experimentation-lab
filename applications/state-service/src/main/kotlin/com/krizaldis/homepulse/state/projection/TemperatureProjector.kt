package com.krizaldis.homepulse.state.projection

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventType
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.state.domain.ProjectionCommand
import com.krizaldis.homepulse.state.domain.ProjectionValue
import org.springframework.stereotype.Component

@Component
class TemperatureProjector : EventProjector {

    override fun supports(eventType: String): Boolean =
        eventType == EventType.TEMPERATURE_REPORTED.wireName

    override fun project(
        event: DomainEvent<*>
    ): ProjectionCommand {

        val payload = event.payload as? TemperatureReported
            ?: throw IllegalArgumentException(
                "Expected TemperatureReported payload, got " +
                    event.payload::class.simpleName
            )

        return ProjectionCommand(
            projectionType = "device-state",
            partitionKey = "HOME#${event.metadata.homeId}",
            sortKey = "DEVICE#${event.metadata.deviceId}",
            sequenceNumber = event.metadata.sequenceNumber,
            attributes = buildMap {
                put("homeId", ProjectionValue.StringValue(event.metadata.homeId))
                put("deviceId", ProjectionValue.StringValue(event.metadata.deviceId))
                put("eventId", ProjectionValue.StringValue(event.metadata.eventId))
                put("eventType", ProjectionValue.StringValue(event.metadata.eventType))
                put(
                    "sequenceNumber",
                    ProjectionValue.NumberValue(event.metadata.sequenceNumber)
                )
                put(
                    "temperatureCelsius",
                    ProjectionValue.NumberValue(payload.temperatureCelsius)
                )
                payload.humidityPercent?.let {
                    put(
                        "humidityPercent",
                        ProjectionValue.NumberValue(it)
                    )
                }
                put(
                    "reportedAt",
                    ProjectionValue.StringValue(
                        event.metadata.occurredAt.toString()
                    )
                )
                put(
                    "updatedAt",
                    ProjectionValue.StringValue(
                        event.metadata.occurredAt.toString()
                    )
                )
            }
        )
    }
}
