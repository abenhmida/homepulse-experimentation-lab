package com.krizaldis.homepulse.event

data class DomainEvent<T>(
    val metadata: EventMetadata,
    val payload: T
)
