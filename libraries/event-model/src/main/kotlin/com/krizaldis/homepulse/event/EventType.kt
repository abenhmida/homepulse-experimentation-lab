package com.krizaldis.homepulse.event

enum class EventType(
    val wireName: String
) {
    TEMPERATURE_REPORTED(
        "home.device.temperature-reported"
    ),
    MOTION_DETECTED(
        "home.device.motion-detected"
    ),
    DOOR_STATE_CHANGED(
        "home.device.door-state-changed"
    ),
    LIGHT_STATE_CHANGED(
        "home.device.light-state-changed"
    ),
    ENERGY_MEASURED(
        "home.device.energy-measured"
    ),
    SMOKE_DETECTED(
        "home.device.smoke-detected"
    );

    companion object {
        fun fromWireName(value: String): EventType =
            entries.firstOrNull { it.wireName == value }
                ?: throw UnknownEventTypeException(value)
    }
}