package com.krizaldis.homepulse.events

data class TemperatureMeasured(
    val temperature: Double,
    val unit: TemperatureUnit
)

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}
