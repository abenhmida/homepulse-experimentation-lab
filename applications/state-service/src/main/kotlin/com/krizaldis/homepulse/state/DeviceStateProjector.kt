package com.krizaldis.homepulse.state

import com.krizaldis.homepulse.device.DeviceType
import com.krizaldis.homepulse.event.DeviceTemperatureReported
import com.krizaldis.homepulse.state.model.DeviceState
import java.time.Instant

class DeviceStateProjector {
    fun project(
        event: DeviceTemperatureReported
    ): DeviceState {
        return DeviceState(
            homeId = event.homeId,
            deviceId = event.deviceId,
            deviceType = DeviceType.THERMOSTAT.name,
            temperature = event.temperature,
            lastEventId = event.eventId,
            lastSequenceNumber = event.sequenceNumber,
            lastEventAt = event.occurredAt,
            updatedAt = Instant.now().toString()
        )
    }
}