package com.krizaldis.homepulse.event

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTemperatureReported(
    val eventId: String,
    val homeId: String,
    val deviceId: String,
    val temperature: Double,
    val occurredAt: String
)
