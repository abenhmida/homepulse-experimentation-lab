package com.krizaldis.homepulse.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer

class KafkaConsumerFactory(
    private val properties: KafkaProperties
) {
    fun create(): KafkaConsumer<String, ByteArray> {
        requireNotNull(properties.groupId){
            "groupId is required for consumers"
        }


        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
                    to properties.bootstrapServers,

            ConsumerConfig.CLIENT_ID_CONFIG
                    to properties.clientId,

            ConsumerConfig.GROUP_ID_CONFIG
                    to properties.groupId,

            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
                    to StringDeserializer::class.java,

            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
                    to StringDeserializer::class.java,

            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
                    to properties.autoOffsetReset,

            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG
                    to properties.enableAutoCommit
        )
        return KafkaConsumer(config)
    }
}