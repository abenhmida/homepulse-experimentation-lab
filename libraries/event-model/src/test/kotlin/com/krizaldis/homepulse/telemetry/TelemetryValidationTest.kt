package com.krizaldis.homepulse.telemetry

import com.krizaldis.homepulse.event.TemperatureMeasured
import com.krizaldis.homepulse.event.TemperatureUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TelemetryValidationTest {
    @Test
    fun `temperature outside supported range is rejected`() {

        assertThrows<IllegalArgumentException> {
            TemperatureMeasured(
                temperature = 999.0,
                unit = TemperatureUnit.CELSIUS
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