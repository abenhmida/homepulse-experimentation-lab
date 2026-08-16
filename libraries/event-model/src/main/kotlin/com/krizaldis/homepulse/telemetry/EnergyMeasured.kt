package com.krizaldis.homepulse.telemetry

data class EnergyMeasured(
    val watts: Double,
    val totalWattHours: Double
) {
    init {
        require(watts >= 0) {
            "Power consumption cannot be negative"
        }

        require(totalWattHours >= 0) {
            "Total energy consumption cannot be negative"
        }
    }
}
