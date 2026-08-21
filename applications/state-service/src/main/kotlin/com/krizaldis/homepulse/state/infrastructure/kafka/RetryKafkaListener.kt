package com.krizaldis.homepulse.state.infrastructure.kafka

import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.state.retry.RetryProcessingResult
import com.krizaldis.homepulse.state.retry.RetryProcessor
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class RetryKafkaListener(
    private val retryProcessor: RetryProcessor,
    private val codec: EventJsonCodec,
    private val chaosFailureInjector: ChaosFailureInjector
) {

    @KafkaListener(
        topics = ["\${homepulse.kafka.topics.retry}"],
        groupId = "\${homepulse.kafka.consumer.retry-group-id}"
    )
    fun consume(
        record: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        val envelope: RetryEnvelope = codec.deserializeRetryEnvelope(record.value())

        when (retryProcessor.process(envelope)) {
            RetryProcessingResult.Processed -> acknowledge(acknowledgment)
            is RetryProcessingResult.RetryScheduled -> acknowledge(acknowledgment)
            is RetryProcessingResult.DeadLettered -> acknowledge(acknowledgment)
            is RetryProcessingResult.NotReady ->
                throw RetryNotReadyException(envelope.retry.nextAttemptAt.toString())
        }
    }

    private fun acknowledge(acknowledgment: Acknowledgment) {
        chaosFailureInjector.beforeAcknowledgement()
        acknowledgment.acknowledge()
    }
}

class RetryNotReadyException(message: String) : RuntimeException(
    "Retry event is not ready yet: $message"
)
