package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.event.retry.RetryEnvelope

/**
 * Application port for publishing a retry message.
 *
 * The port deliberately exposes no Kafka types, topic names, headers or
 * ProducerRecord. The application only decides whether a retry or a DLQ
 * publication is required; the Kafka adapter owns the transport details.
 */
interface RetryMessagePublisher {
    fun publishRetry(envelope: RetryEnvelope): RetryPublicationResult

    fun publishDeadLetter(envelope: RetryEnvelope): RetryPublicationResult
}
