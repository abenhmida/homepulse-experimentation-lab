package com.krizaldis.homepulse.events

import com.krizaldis.homepulse.event.EventFactory
import com.krizaldis.homepulse.event.EventType
import com.krizaldis.homepulse.event.TemperatureUnit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class EventFactoryTest {

    @Test
    fun `should create temperature event`() {
        val clock = Clock.fixed(
            Instant.parse("2026-08-15T15:00:00Z"),
            ZoneOffset.UTC
        )

        val factory = EventFactory(clock)
        val event = factory.temperatureMeasured(
            homeId = "home-001",
            deviceId = "thermostat-living-room-01",
            temperature = 21.7,
            unit = TemperatureUnit.CELSIUS
        )

        Assertions.assertNotNull(event.metadata.eventId)
        assertEquals(
            EventType.TEMPERATURE_MEASURED,
            event.metadata.eventType
        )

        assertEquals(
            1,
            event.metadata.eventVersion
        )

        assertEquals(
            "home-001",
            event.metadata.homeId
        )

        assertEquals(
            "thermostat-living-room-01",
            event.metadata.deviceId
        )

        assertEquals(
            21.7,
            event.payload.temperature
        )

        assertEquals(
            TemperatureUnit.CELSIUS,
            event.payload.unit
        )
    }
}