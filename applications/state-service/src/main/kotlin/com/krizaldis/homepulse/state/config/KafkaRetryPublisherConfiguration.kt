package com.krizaldis.homepulse.state.config

import com.krizaldis.homepulse.kafka.EventPublisher
import com.krizaldis.homepulse.kafka.KafkaEventPublisher
import com.krizaldis.homepulse.kafka.KafkaProducerFactory
import com.krizaldis.homepulse.kafka.KafkaProperties
import com.krizaldis.homepulse.kafka.retry.KafkaRetryTopicStrategy
import com.krizaldis.homepulse.state.infrastructure.kafka.KafkaRetryMessagePublisher
import com.krizaldis.homepulse.state.retry.RetryMessagePublisher
import org.apache.kafka.clients.producer.KafkaProducer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KafkaPublisherProperties::class)
class KafkaRetryPublisherConfiguration {

    @Bean
    fun kafkaProperties(
        properties: KafkaPublisherProperties
    ): KafkaProperties = KafkaProperties(
        bootstrapServers = properties.bootstrapServers,
        clientId = properties.clientId,
        acks = properties.acks,
        enableIdempotence = properties.enableIdempotence,
        maxInFlightRequests = properties.maxInFlightRequests,
        retries = properties.retries,
        deliveryTimeoutMs = properties.deliveryTimeoutMs,
        requestTimeoutMs = properties.requestTimeoutMs,
        lingerMs = properties.lingerMs,
        batchSize = properties.batchSize
    )

    @Bean(destroyMethod = "close")
    fun kafkaProducer(
        properties: KafkaProperties
    ): KafkaProducer<String, String> =
        KafkaProducerFactory(properties).create()

    @Bean
    fun eventPublisher(
        producer: KafkaProducer<String, String>
    ): EventPublisher = KafkaEventPublisher(producer)

    @Bean
    fun kafkaRetryTopicStrategy(): KafkaRetryTopicStrategy =
        KafkaRetryTopicStrategy()

    @Bean
    fun retryMessagePublisher(
        eventPublisher: EventPublisher,
        topicStrategy: KafkaRetryTopicStrategy
    ): RetryMessagePublisher =
        KafkaRetryMessagePublisher(
            eventPublisher = eventPublisher,
            topicStrategy = topicStrategy
        )
}

@ConfigurationProperties(prefix = "homepulse.kafka")
data class KafkaPublisherProperties(
    var bootstrapServers: String = "localhost:9092",
    var clientId: String = "homepulse-state-service",
    var acks: String = "all",
    var enableIdempotence: Boolean = true,
    var maxInFlightRequests: Int = 5,
    var retries: Int = Int.MAX_VALUE,
    var deliveryTimeoutMs: Int = 120_000,
    var requestTimeoutMs: Int = 30_000,
    var lingerMs: Int = 5,
    var batchSize: Int = 32_768
)
