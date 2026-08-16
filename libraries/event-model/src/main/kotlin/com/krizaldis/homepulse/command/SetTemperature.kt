package com.krizaldis.homepulse.command

data class SetTemperature(
    val temperature: Double
) {
    init {
        require(temperature in 5.0..35.0) {
            "Target temperature must be between 5 and 35 degrees Celsius"
        }
    }
}
