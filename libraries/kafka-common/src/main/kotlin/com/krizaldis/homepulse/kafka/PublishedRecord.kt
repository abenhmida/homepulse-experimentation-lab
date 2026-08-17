package com.krizaldis.homepulse.kafka

data class PublishedRecord(
    val topic: String,
    val partition: Int,
    val offset: Long,
    val timestamp: Long?
)