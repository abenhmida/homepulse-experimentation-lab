package com.krizaldis.homepulse.event

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTemperatureReported(
    val eventId: String,
    val homeId: String,
    val deviceId: String,
    val sequenceNumber: Long,
    val temperature: Double,
    val occurredAt: String
)
