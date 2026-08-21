package com.krizaldis.homepulse.state.kafka

import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.state.retry.KafkaRetryTopicStrategy
import com.krizaldis.homepulse.state.retry.PublishResult
import com.krizaldis.homepulse.state.retry.RetryPublisher
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.CompletionStage

@Component
class KafkaRetryPublisher(
    private val kafkaTemplate: KafkaTemplate<String, RetryEnvelope>,
    private val topicStrategy: KafkaRetryTopicStrategy
) : RetryPublisher {
    override fun publish(envelope: RetryEnvelope): CompletionStage<PublishResult> {
        val topic = topicStrategy.topicFor(envelope)

        return kafkaTemplate.send(
            topic,
            envelope.event.metadata.eventId,
            envelope
        )
            .handle { _, throwable ->
                if (throwable != null) {
                    PublishResult.Failed(throwable)
                } else {
                    PublishResult.Published
                }
            }
    }
}