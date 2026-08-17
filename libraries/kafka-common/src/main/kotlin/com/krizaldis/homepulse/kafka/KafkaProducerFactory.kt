package com.krizaldis.homepulse.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

open class KafkaProducerFactory(
    private val properties: KafkaProperties
) {
    open fun create(): KafkaProducer<String, String> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to properties.bootstrapServers,
            ProducerConfig.CLIENT_ID_CONFIG
                    to properties.clientId,

            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
                    to StringSerializer::class.java,

            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
                    to StringSerializer::class.java,

            ProducerConfig.ACKS_CONFIG
                    to properties.acks,

            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG
                    to properties.enableIdempotence,

            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION
                    to properties.maxInFlightRequests,

            ProducerConfig.RETRIES_CONFIG
                    to properties.retries,

            ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG
                    to properties.deliveryTimeoutMs,

            ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG
                    to properties.requestTimeoutMs,

            ProducerConfig.LINGER_MS_CONFIG
                    to properties.lingerMs,

            ProducerConfig.BATCH_SIZE_CONFIG
                    to properties.batchSize
        )
        return KafkaProducer<String, String>(config)
    }
}