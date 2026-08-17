package com.krizaldis.homepulse.state.application

import com.krizaldis.homepulse.event.DeviceTemperatureReported
import com.krizaldis.homepulse.state.domain.DeviceStateRepository
import com.krizaldis.homepulse.state.domain.ProjectionResult

class DeviceStateProjection(
    private val repository: DeviceStateRepository
) : StateProjection {
    override suspend fun apply(event: DeviceTemperatureReported): ProjectionResult {
        return repository.apply(event)
    }
}