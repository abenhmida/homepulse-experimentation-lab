package com.krizaldis.homepulse.telemetry

data class DoorStateChanged(
    val state: DoorState
)

enum class DoorState {
    OPEN,
    CLOSED
}