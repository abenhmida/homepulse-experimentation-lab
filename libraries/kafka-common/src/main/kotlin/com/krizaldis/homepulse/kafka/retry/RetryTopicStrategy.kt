package com.krizaldis.homepulse.kafka.retry

/**
 * Kafka-specific mapping from the original topic to retry/DLQ topics.
 *
 * Topic naming is deliberately kept out of state-service because a topic is
 * a Kafka transport concept, not an application-level retry decision.
 */
data class RetryTopicStrategy(
    val retryTopicSeparator: String = ".retry.",
    val dlqSuffix: String = ".dlq"
) {
    fun retryTopic(originalTopic: String, attempt: Int): String {
        require(originalTopic.isNotBlank()) {
            "Original topic must not be blank"
        }
        require(attempt >= 1) {
            "Retry attempt must be >= 1"
        }

        return "$originalTopic$retryTopicSeparator$attempt"
    }

    fun dlqTopic(originalTopic: String): String {
        require(originalTopic.isNotBlank()) {
            "Original topic must not be blank"
        }

        return "$originalTopic$dlqSuffix"
    }
}
