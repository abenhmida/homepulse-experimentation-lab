package com.krizaldis.homepulse.kafka.retry

import com.krizaldis.homepulse.event.retry.RetryMetadata

object KafkaRetryHeaderMapper {

    fun toHeaders(metadata: RetryMetadata): Map<String, String> =
        buildMap {
            put(KafkaRetryHeaders.ATTEMPT, metadata.attempt.toString())
            put(KafkaRetryHeaders.ORIGINAL_EVENT_ID, metadata.originalEventId)
            put(KafkaRetryHeaders.ORIGINAL_TOPIC, metadata.originalTopic)
            put(
                KafkaRetryHeaders.ORIGINAL_PARTITION,
                metadata.originalPartition.toString()
            )
            put(
                KafkaRetryHeaders.ORIGINAL_OFFSET,
                metadata.originalOffset.toString()
            )
            put(KafkaRetryHeaders.FAILURE_TYPE, metadata.failureType.name)
            put(KafkaRetryHeaders.EXCEPTION_TYPE, metadata.exceptionType)
            put(
                KafkaRetryHeaders.FIRST_FAILED_AT,
                metadata.firstFailedAt.toString()
            )
            put(
                KafkaRetryHeaders.LAST_FAILED_AT,
                metadata.lastFailedAt.toString()
            )
            put(
                KafkaRetryHeaders.NEXT_ATTEMPT_AT,
                metadata.nextAttemptAt.toString()
            )

            metadata.correlationId?.let {
                put(KafkaRetryHeaders.CORRELATION_ID, it)
            }

            metadata.causationId?.let {
                put(KafkaRetryHeaders.CAUSATION_ID, it)
            }
        }
}
