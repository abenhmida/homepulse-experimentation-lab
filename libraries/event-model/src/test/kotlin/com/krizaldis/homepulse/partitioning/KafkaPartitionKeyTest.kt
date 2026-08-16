package com.krizaldis.homepulse.partitioning

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KafkaPartitionKeyTest {

    @Test
    fun `same home and device produce same partition key`() {

        val first =
            KafkaPartitionKey.forDevice(
                homeId = "home-001",
                deviceId = "thermostat-01"
            )

        val second =
            KafkaPartitionKey.forDevice(
                homeId = "home-001",
                deviceId = "thermostat-01"
            )

        assertEquals(first, second)
    }

    @Test
    fun `different devices produce different keys`() {

        val thermostat =
            KafkaPartitionKey.forDevice(
                "home-001",
                "thermostat-01"
            )

        val light =
            KafkaPartitionKey.forDevice(
                "home-001",
                "light-01"
            )

        assert(thermostat != light)
    }
}