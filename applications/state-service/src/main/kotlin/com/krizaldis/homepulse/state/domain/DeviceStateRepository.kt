package com.krizaldis.homepulse.state.domain

import com.krizaldis.homepulse.event.DeviceTemperatureReported

interface DeviceStateRepository {
    suspend fun apply(event: DeviceTemperatureReported): ProjectionResult
}
