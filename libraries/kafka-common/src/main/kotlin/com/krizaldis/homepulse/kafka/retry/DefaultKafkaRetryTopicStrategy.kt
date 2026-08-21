package com.krizaldis.homepulse.kafka.retry

import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.state.retry.KafkaRetryTopicStrategy

class DefaultKafkaRetryTopicStrategy(
    private val baseTopic: String
) : KafkaRetryTopicStrategy {

    private val retryTopicStrategy = RetryTopicStrategy()

    override fun topicFor(envelope: RetryEnvelope): String {
        return when (envelope.retry.attempt) {
            1, 2, 3 -> retryTopicStrategy
                .retryTopic(
                    baseTopic,
                    attempt = envelope.retry.attempt
                )

            else -> retryTopicStrategy
                .dlqTopic(baseTopic)
        }
    }
}