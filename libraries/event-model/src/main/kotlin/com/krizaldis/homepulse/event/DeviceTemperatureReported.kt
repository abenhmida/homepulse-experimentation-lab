package com.krizaldis.homepulse.event

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class DeviceTemperatureReported (
    @JsonProperty("eventId") val eventId: String,
    @JsonProperty("homeId")val homeId: String,
    @JsonProperty("deviceId")val deviceId: String,
    @JsonProperty("sequenceNumber")val sequenceNumber: Long,
    @JsonProperty("temperature")val temperature: Double,
    @JsonProperty("occurredAt") val occurredAt: String
)
