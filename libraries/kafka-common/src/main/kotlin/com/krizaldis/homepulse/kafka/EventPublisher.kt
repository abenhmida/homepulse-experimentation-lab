package com.krizaldis.homepulse.kafka

interface EventPublisher {
    suspend fun publish(event: PublishedEvent): PublishedRecord
}