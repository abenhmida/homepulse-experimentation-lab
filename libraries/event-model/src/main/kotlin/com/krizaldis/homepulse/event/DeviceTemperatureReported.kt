package com.krizaldis.homepulse.event

import com.fasterxml.jackson.annotation.JsonProperty

data class DeviceTemperatureReported (
    @param:JsonProperty("eventId") val eventId: String,
    @param:JsonProperty("homeId")val homeId: String,
    @param:JsonProperty("deviceId")val deviceId: String,
    @param:JsonProperty("sequenceNumber")val sequenceNumber: Long,
    @param:JsonProperty("temperature")val temperature: Double,
    @param:JsonProperty("occurredAt") val occurredAt: String
)
