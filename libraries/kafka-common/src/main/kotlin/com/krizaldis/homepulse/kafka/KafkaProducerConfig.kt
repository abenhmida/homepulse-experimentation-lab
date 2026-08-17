package com.krizaldis.homepulse.kafka

import org.apache.kafka.clients.producer.ProducerConfig

object KafkaProducerConfig {
    fun defaults(
        bootstrapServers: String,
        clientId: String
    ): Map<String, Any> = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        ProducerConfig.CLIENT_ID_CONFIG to clientId,
        ProducerConfig.ACKS_CONFIG to "all",
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
        ProducerConfig.RETRIES_CONFIG to Int.MAX_VALUE,
        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 5,
        ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG to 120_000,
        ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to 30_000,
        ProducerConfig.LINGER_MS_CONFIG to 5,
        ProducerConfig.BATCH_SIZE_CONFIG to 32 * 1024,
    )
}