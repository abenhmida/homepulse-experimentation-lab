package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.event.retry.RetryEnvelope

interface KafkaRetryTopicStrategy {
    fun topicFor(envelope: RetryEnvelope): String
}