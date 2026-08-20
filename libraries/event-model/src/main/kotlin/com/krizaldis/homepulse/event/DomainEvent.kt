package com.krizaldis.homepulse.event

import com.fasterxml.jackson.annotation.JsonProperty

data class DomainEvent<T>(
    val metadata: EventMetadata,
    val payload: T
) {
}

data class TemperatureReported (
    @param:JsonProperty("temperatureCelsius") val temperatureCelsius: Double,
    @param:JsonProperty("humidityPercent") val humidityPercent: Double? = null
){
    init {
        require(temperatureCelsius in -100.0..100.0) {
            "Temperature is outside supported range"
        }

        humidityPercent?.let {
            require(it in 0.0..100.0) {
                "Humidity must be between 0 and 100"
            }
        }
    }

}


data class LightStateChanged(
    val on: Boolean,
    val brightnessPercent: Int
){
    init {
        require(brightnessPercent in 0..100) {
            "Brightness must be between 0 and 100"
        }
    }
}

data class DoorStateChanged(
    val open: Boolean
)


data class EnergyMeasured(
    val watts: Double,
    val totalWattHours: Double
) {
    init {
        require(watts >= 0) {
            "watts cannot be negative"
        }

        require(totalWattHours >= 0) {
            "totalWattHours cannot be negative"
        }
    }
}

object EventTypes {
    const val TEMPERATURE_READING = "home.device.temperature.v1"
    const val LIGHT_STATE_CHANGED = "home.device.light-state.v1"
    const val DOOR_STATE_CHANGED = "home.device.door-state.v1"
}