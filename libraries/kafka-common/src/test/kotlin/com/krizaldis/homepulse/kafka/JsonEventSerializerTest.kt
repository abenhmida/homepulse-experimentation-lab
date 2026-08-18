package com.krizaldis.homepulse.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.krizaldis.homepulse.event.DeviceTemperatureReported
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonEventSerializerTest {

    private val sut = JsonEventSerializer()
    private val mapper = ObjectMapper()

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
            mapper.readValue(json, DeviceTemperatureReported::class.java).deviceId.contains("thermostat-1")
        )
    }
}