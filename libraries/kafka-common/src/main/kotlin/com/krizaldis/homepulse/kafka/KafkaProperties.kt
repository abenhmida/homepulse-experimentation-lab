package com.krizaldis.homepulse.kafka

data class KafkaProperties(
    val bootstrapServers: String,
    val clientId: String,
    val groupId: String? = null,
    val acks: String = "all",
    val enableIdempotence: Boolean = true,
    val maxInFlightRequests: Int = 5,
    val retries: Int = Int.MAX_VALUE,

    val deliveryTimeoutMs: Int = 120_000,

    val requestTimeoutMs: Int = 30_000,

    val lingerMs: Int = 5,

    val batchSize: Int = 32_768,

    val autoOffsetReset: String = "earliest",

    val enableAutoCommit: Boolean = false
)
