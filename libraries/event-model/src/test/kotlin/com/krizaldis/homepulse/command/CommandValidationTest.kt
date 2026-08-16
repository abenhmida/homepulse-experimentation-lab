package com.krizaldis.homepulse.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CommandValidationTest {
    @Test
    fun `invalid target temperature is rejected`() {

        assertThrows<IllegalArgumentException> {
            SetTemperature(-10.0)
        }
    }

    @Test
    fun `brightness above 100 is rejected`() {

        assertThrows<IllegalArgumentException> {
            SetBrightness(101)
        }
    }
}