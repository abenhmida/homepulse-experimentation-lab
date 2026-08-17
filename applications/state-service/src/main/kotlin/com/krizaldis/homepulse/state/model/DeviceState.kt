package com.krizaldis.homepulse.state.model

data class DeviceState(
    val homeId: String,
    val deviceId: String,
    val deviceType: String,
    val temperature: Double?,
    val lastEventId: String,
    val lastSequenceNumber: Long,
    val lastEventAt: String,
    val updatedAt: String
)