package com.krizaldis.homepulse.state.projection

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventMetadata
import com.krizaldis.homepulse.event.EventType
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.state.domain.UnsupportedEventException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class ProjectionDispatcherTest {

    private val temperatureProjector =
        TemperatureProjector()

    private val dispatcher =
        ProjectionDispatcher(
            projectors = listOf(
                temperatureProjector
            )
        )

    @Test
    fun `temperature event is routed to temperature projector`() {

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

        val result =
            dispatcher.dispatch(event)

        assertEquals(
            "device-state",
            result.projectionType
        )

        assertEquals(
            "HOME#home-001",
            result.partitionKey
        )

        assertEquals(
            "DEVICE#thermostat-01",
            result.sortKey
        )

        assertEquals(
            42,
            result.sequenceNumber
        )
    }

    @Test
    fun `unsupported event type is rejected`() {

        val event =
            DomainEvent(
                metadata =
                    EventMetadata(
                        eventId = "evt-002",
                        eventType = "home.device.unknown",
                        schemaVersion = 1,
                        homeId = "home-001",
                        deviceId = "device-01",
                        occurredAt = Instant.now(),
                        sequenceNumber = 1
                    ),
                payload = "unknown"
            )

        assertThrows<UnsupportedEventException> {
            dispatcher.dispatch(event)
        }
    }
}
