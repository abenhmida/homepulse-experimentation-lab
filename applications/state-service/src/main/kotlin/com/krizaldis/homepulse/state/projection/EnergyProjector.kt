package com.krizaldis.homepulse.state.projection

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventType
import com.krizaldis.homepulse.event.EnergyMeasured
import com.krizaldis.homepulse.state.domain.ProjectionCommand
import com.krizaldis.homepulse.state.domain.ProjectionValue
import org.springframework.stereotype.Component

@Component
class EnergyProjector : EventProjector {

    override fun supports(eventType: String): Boolean =
        eventType == EventType.ENERGY_MEASURED.wireName

    override fun project(
        event: DomainEvent<*>
    ): ProjectionCommand {

        val payload = event.payload as? EnergyMeasured
            ?: throw IllegalArgumentException(
                "Expected EnergyMeasured payload, got " +
                    event.payload::class.simpleName
            )

        return ProjectionCommand(
            projectionType = "device-state",
            partitionKey = "HOME#${event.metadata.homeId}",
            sortKey = "DEVICE#${event.metadata.deviceId}",
            sequenceNumber = event.metadata.sequenceNumber,
            attributes = mapOf(
                "homeId" to ProjectionValue.StringValue(event.metadata.homeId),
                "deviceId" to ProjectionValue.StringValue(event.metadata.deviceId),
                "eventId" to ProjectionValue.StringValue(event.metadata.eventId),
                "eventType" to ProjectionValue.StringValue(event.metadata.eventType),
                "sequenceNumber" to ProjectionValue.NumberValue(
                    event.metadata.sequenceNumber
                ),
                "watts" to ProjectionValue.NumberValue(payload.watts),
                "totalWattHours" to ProjectionValue.NumberValue(
                    payload.totalWattHours
                ),
                "reportedAt" to ProjectionValue.StringValue(
                    event.metadata.occurredAt.toString()
                ),
                "updatedAt" to ProjectionValue.StringValue(
                    event.metadata.occurredAt.toString()
                )
            )
        )
    }
}
