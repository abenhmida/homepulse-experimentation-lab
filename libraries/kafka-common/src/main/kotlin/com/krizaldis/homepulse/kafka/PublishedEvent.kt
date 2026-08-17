package com.krizaldis.homepulse.kafka

data class PublishedEvent(
    val topic: String,
    val key: String,
    val eventType: String,
    val payload: String,
    val headers: Map<String, String> = emptyMap()
)
