package com.krizaldis.homepulse.command

import java.time.Instant

data class CommandMetadata(
    val commandId: String,
    val commandType: CommandType,
    val commandVersion: Int,
    val createdAt: Instant,
    val homeId: String,
    val targetDeviceId: String,
    val correlationId: String,
    val causationId: String?,
    val source: String
)