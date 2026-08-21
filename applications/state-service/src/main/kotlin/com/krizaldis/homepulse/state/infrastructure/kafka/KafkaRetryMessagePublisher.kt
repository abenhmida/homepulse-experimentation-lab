package com.krizaldis.homepulse.state.infrastructure.kafka

import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.kafka.EventPublisher
import com.krizaldis.homepulse.kafka.KafkaEventHeaders
import com.krizaldis.homepulse.kafka.PublishedEvent
import com.krizaldis.homepulse.kafka.PublishedRecord
import com.krizaldis.homepulse.kafka.retry.KafkaRetryHeaderMapper
import com.krizaldis.homepulse.kafka.retry.KafkaRetryTopicStrategy
import com.krizaldis.homepulse.partitioning.KafkaPartitionKey
import com.krizaldis.homepulse.serialization.JsonMapper
import com.krizaldis.homepulse.state.retry.RetryMessagePublisher
import com.krizaldis.homepulse.state.retry.RetryPublicationResult

/**
 * Kafka adapter for the retry publication port.
 *
 * Responsibilities are intentionally limited to transport concerns:
 * - choose the Kafka retry/DLQ topic;
 * - serialize the retry envelope;
 * - map retry metadata to Kafka headers;
 * - delegate the actual send to the shared EventPublisher.
 *
 * It does not evaluate retry policy and it does not decide whether an event
 * should be retried. Those decisions belong to state-service application code.
 */
class KafkaRetryMessagePublisher(
    private val eventPublisher: EventPublisher,
    private val topicStrategy: KafkaRetryTopicStrategy = KafkaRetryTopicStrategy()
) : RetryMessagePublisher {

    override suspend fun publishRetry(
        envelope: RetryEnvelope
    ): RetryPublicationResult {
        val topic = topicStrategy.retryTopic(
            originalTopic = envelope.retry.originalTopic,
            attempt = envelope.retry.attempt
        )

        return publish(
            envelope = envelope,
            topic = topic
        )
    }

    override suspend fun publishDeadLetter(
        envelope: RetryEnvelope
    ): RetryPublicationResult {
        val topic = topicStrategy.dlqTopic(
            originalTopic = envelope.retry.originalTopic
        )

        return publish(
            envelope = envelope,
            topic = topic
        )
    }

    private suspend fun publish(
        envelope: RetryEnvelope,
        topic: String
    ): RetryPublicationResult {
        val eventMetadata = envelope.event.metadata

        val headers = buildMap {
            put(KafkaEventHeaders.EVENT_ID, eventMetadata.eventId)
            put(
                KafkaEventHeaders.EVENT_VERSION,
                eventMetadata.schemaVersion.toString()
            )

            eventMetadata.correlationId?.let {
                put(KafkaEventHeaders.CORRELATION_ID, it)
            }

            eventMetadata.causationId?.let {
                put(KafkaEventHeaders.CAUSATION_ID, it)
            }

            putAll(KafkaRetryHeaderMapper.toHeaders(envelope.retry))
        }

        val payload = JsonMapper.mapper.writeValueAsString(envelope)

        val published = eventPublisher.publish(
            PublishedEvent(
                topic = topic,
                key = KafkaPartitionKey.forDevice(
                    homeId = eventMetadata.homeId,
                    deviceId = eventMetadata.deviceId
                ).value,
                eventType = eventMetadata.eventType,
                payload = payload,
                headers = headers
            )
        )

        return RetryPublicationResult(
            topic = published.topic,
            partition = published.partition,
            offset = published.offset
        )
    }
}
