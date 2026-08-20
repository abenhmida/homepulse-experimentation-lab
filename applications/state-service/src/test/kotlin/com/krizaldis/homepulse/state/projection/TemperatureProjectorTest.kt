package com.krizaldis.homepulse.state.projection

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventMetadata
import com.krizaldis.homepulse.event.EventType
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.state.domain.ProjectionValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class TemperatureProjectorTest {

    private val projector = TemperatureProjector()

    @Test
    fun `projects temperature event into generic projection command`() {

        val event =
            DomainEvent(
                metadata =
                    EventMetadata(
                        eventId = "evt-001",
                        eventType =
                            EventType.TEMPERATURE_REPORTED.wireName,
                        schemaVersion = 1,
                        homeId = "home-001",
                        deviceId = "thermostat-01",
                        occurredAt =
                            Instant.parse(
                                "2026-08-20T15:00:00Z"
                            ),
                        sequenceNumber = 42
                    ),
                payload =
                    TemperatureReported(
                        temperatureCelsius = 21.5,
                        humidityPercent = 47.0
                    )
            )

        val projection =
            projector.project(event)

        assertEquals(
            ProjectionValue.NumberValue(21.5),
            projection.attributes["temperatureCelsius"]
        )

        assertEquals(
            ProjectionValue.NumberValue(47.0),
            projection.attributes["humidityPercent"]
        )
    }
}
