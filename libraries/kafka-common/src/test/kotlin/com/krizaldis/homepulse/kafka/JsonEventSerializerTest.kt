package com.krizaldis.homepulse.kafka

import com.krizaldis.homepulse.event.DeviceTemperatureReported
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonEventSerializerTest {

    private val sut = JsonEventSerializer()

    @Test
    fun `should serialize event`() {
        val event =
            DeviceTemperatureReported(
                eventId = "event-1",
                homeId = "home-1",
                deviceId = "thermostat-1",
                sequenceNumber = 1L,
                temperature = 21.5,
                occurredAt = "2026-08-16T10:00:00Z"
            )

        val json = sut.serialize(event)

        assertTrue(
            json.contains("thermostat-1")
        )
    }
}