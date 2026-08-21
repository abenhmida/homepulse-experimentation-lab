package com.krizaldis.homepulse.kafka

interface EventPublisher {
    fun publish(event: PublishedEvent): PublishedRecord
}