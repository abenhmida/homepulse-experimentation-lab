package com.krizaldis.homepulse.event

data class DomainEvent<T>(
    val metadata: EventMetadata,
    val payload: T
) {
}

data class TemperatureReading(
    val temperatureCelsius: Double,
    val humidityPercent: Double
)

data class LightStateChanged(
    val on: Boolean,
    val brightnessPercent: Int
)

data class DoorStateChanged(
    val open: Boolean
)

object EventTypes {
    const val TEMPERATURE_READING = "home.device.temperature.v1"
    const val LIGHT_STATE_CHANGED = "home.device.light-state.v1"
    const val DOOR_STATE_CHANGED = "home.device.door-state.v1"
}