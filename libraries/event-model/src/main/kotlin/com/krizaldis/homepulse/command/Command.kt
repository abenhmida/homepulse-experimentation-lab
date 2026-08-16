package com.krizaldis.homepulse.command

data class Command<T>(
    val metadata: CommandMetadata,
    val payload: T
)