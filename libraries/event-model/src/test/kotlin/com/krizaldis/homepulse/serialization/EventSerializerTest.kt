package com.krizaldis.homepulse.serialization

import com.krizaldis.homepulse.device.DeviceId
import com.krizaldis.homepulse.device.HomeId
import com.krizaldis.homepulse.event.EventFactory
import com.krizaldis.homepulse.event.TemperatureMeasured
import com.krizaldis.homepulse.event.TemperatureUnit
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class EventSerializerTest {

    @Test
    fun `should serialize and deserialize temperature event`() {

        val clock = Clock.fixed(
            Instant.parse("2026-08-15T15:00:00Z"),
            ZoneOffset.UTC
        )

        val event = EventFactory(clock)
            .temperatureMeasured(
                homeId = HomeId("home-001"),
                deviceId = DeviceId("thermostat-01"),
                temperature = 21.7,
                unit = TemperatureUnit.CELSIUS
            )

        val serializer = EventSerializer()

        val bytes = serializer.serialize(event)

        val restored =
            serializer.deserialize(
                bytes,
                TemperatureMeasured::class.java
            )

        assertEquals(
            event.metadata.eventId,
            restored.metadata.eventId
        )

        assertEquals(
            event.metadata.eventType,
            restored.metadata.eventType
        )

        assertEquals(
            event.payload.temperature,
            restored.payload.temperature
        )

        assertEquals(
            event.payload.unit,
            restored.payload.unit
        )
    }
}