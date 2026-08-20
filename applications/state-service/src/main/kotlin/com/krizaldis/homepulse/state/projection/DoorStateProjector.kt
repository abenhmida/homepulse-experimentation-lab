package com.krizaldis.homepulse.state.projection

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventType
import com.krizaldis.homepulse.event.DoorStateChanged
import com.krizaldis.homepulse.state.domain.ProjectionCommand
import com.krizaldis.homepulse.state.domain.ProjectionValue
import org.springframework.stereotype.Component

@Component
class DoorStateProjector : EventProjector {

    override fun supports(eventType: String): Boolean =
        eventType == EventType.DOOR_STATE_CHANGED.wireName

    override fun project(
        event: DomainEvent<*>
    ): ProjectionCommand {

        val payload = event.payload as? DoorStateChanged
            ?: throw IllegalArgumentException(
                "Expected DoorStateChanged payload, got " +
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
                "doorOpen" to ProjectionValue.BooleanValue(payload.open),
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
