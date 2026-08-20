package com.krizaldis.homepulse.events
import com.krizaldis.homepulse.event.EventFactory
import com.krizaldis.homepulse.event.EventMetadata
import com.krizaldis.homepulse.event.EventType
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.event.UnknownEventTypeException
import com.krizaldis.homepulse.serialization.EventSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

class EventSerializationTest {

    private val serializer = EventSerializer()

    @Test
    fun `temperature event survives serialization round trip`() {

        val event =
            EventFactory.temperatureReported(
                eventId = UUID.randomUUID().toString(),
                homeId = "home-001",
                deviceId = "thermostat-01",
                sequenceNumber = 42,
                occurredAt = Instant.parse(
                    "2026-08-20T15:00:00Z"
                ),
                temperatureCelsius = 21.5,
                humidityPercent = 47.0,
                correlationId = "corr-001"
            )

        val bytes = serializer.serialize(event)

        val restored =
            serializer.deserialize(
                bytes,
                TemperatureReported::class.java
            )

        assertEquals(event, restored)
    }

    @Test
    fun `serialized event is valid utf8 json`() {

        val event =
            EventFactory.temperatureReported(
                eventId = "evt-001",
                homeId = "home-001",
                deviceId = "thermostat-01",
                sequenceNumber = 1,
                occurredAt = Instant.parse(
                    "2026-08-20T15:00:00Z"
                ),
                temperatureCelsius = 21.5
            )

        val bytes = serializer.serialize(event)

        val json =
            bytes.toString(Charsets.UTF_8)

        assert(json.startsWith("{"))
        assert(json.contains("\"metadata\""))
        assert(json.contains("\"payload\""))
    }

    @Test
    fun `schema version must be positive`() {

        assertThrows<IllegalArgumentException> {

            EventMetadata(
                eventId = "evt-001",
                eventType = "home.device.temperature-reported",
                schemaVersion = 0,
                homeId = "home-001",
                deviceId = "thermostat-01",
                occurredAt = Instant.now(),
                sequenceNumber = 1
            )
        }
    }

    @Test
    fun `humidity above 100 is rejected`() {

        assertThrows<IllegalArgumentException> {

            TemperatureReported(
                temperatureCelsius = 21.0,
                humidityPercent = 101.0
            )
        }
    }

    @Test
    fun `unknown event type is rejected`() {

        assertThrows<UnknownEventTypeException> {

            EventType.fromWireName(
                "home.device.future-event"
            )
        }
    }
}