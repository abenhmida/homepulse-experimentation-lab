package com.krizaldis.homepulse.command

import java.time.Clock
import java.time.Instant
import java.util.UUID

class CommandFactory(
    private val clock: Clock = Clock.systemUTC()
) {

    fun setTemperature(
        homeId: String,
        deviceId: String,
        temperature: Double,
        correlationId: String = UUID.randomUUID().toString(),
        causationId: String? = null,
        source: String = "homepulse"
    ): Command<SetTemperature> {

        return Command(
            metadata = CommandMetadata(
                commandId = UUID.randomUUID().toString(),
                commandType = CommandType.SET_TEMPERATURE,
                commandVersion = 1,
                createdAt = clock.instant(),
                homeId = homeId,
                targetDeviceId = deviceId,
                correlationId = correlationId,
                causationId = causationId,
                source = source
            ),
            payload = SetTemperature(temperature)
        )
    }
}