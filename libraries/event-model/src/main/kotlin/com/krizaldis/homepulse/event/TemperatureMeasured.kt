package com.krizaldis.homepulse.event

data class TemperatureMeasured(
    val temperature: Double,
    val unit: TemperatureUnit
) {
    init {
        require(temperature in -100.0..100.0) {
            "Temperature is outside the supported range"
        }
    }
}

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}
