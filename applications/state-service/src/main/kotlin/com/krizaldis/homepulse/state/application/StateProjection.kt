package com.krizaldis.homepulse.state.application

import com.krizaldis.homepulse.event.DeviceTemperatureReported
import com.krizaldis.homepulse.state.domain.ProjectionResult

interface StateProjection {
    suspend fun apply(
        event: DeviceTemperatureReported
    ): ProjectionResult
}