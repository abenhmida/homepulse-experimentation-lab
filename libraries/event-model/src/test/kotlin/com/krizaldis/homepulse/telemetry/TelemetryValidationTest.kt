package com.krizaldis.homepulse.telemetry


import com.krizaldis.homepulse.event.TemperatureReported
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TelemetryValidationTest {
    @Test
    fun `temperature outside supported range is rejected`() {

        assertThrows<IllegalArgumentException> {
            TemperatureReported(
                temperatureCelsius = 999.0,
                humidityPercent = 5.5
            )
        }
    }

    @Test
    fun `negative energy consumption is rejected`() {

        assertThrows<IllegalArgumentException> {
            EnergyMeasured(
                watts = -10.0,
                totalWattHours = 100.0
            )
        }
    }
}