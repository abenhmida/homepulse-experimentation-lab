package com.krizaldis.homepulse.simulator

data class ProducerConfig (
    val bootstrapServers: String = env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
    val topic: String = env("KAFKA_TOPIC", "home.events.v1"),
    val clientId: String = env("KAFKA_CLIENT_ID", "homepulse-event-producer"),
    val events: Int = env("EVENT_COUNT", "10").toInt(),
    val intervalMs: Long = env("EVENT_INTERVAL_MS", "500").toLong()
)

private fun env(name: String, default: String): String =
    System.getenv(name) ?: default