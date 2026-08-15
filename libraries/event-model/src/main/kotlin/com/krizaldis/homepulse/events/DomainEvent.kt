package com.krizaldis.homepulse.events

data class DomainEvent<T>(
    val metadata: EventMetadata,
    val payload: T
)
