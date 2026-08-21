package com.krizaldis.homepulse.state.infrastructure.kafka

import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.state.retry.RetryProcessingResult
import com.krizaldis.homepulse.state.retry.RetryProcessor
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class RetryKafkaListener(
    private val retryProcessor: RetryProcessor
) {

    @KafkaListener(
        topics = ["\${homepulse.kafka.topics.retry}"],
        groupId = "\${homepulse.kafka.consumer.group-id}"
    )
    fun consume(
        envelope: RetryEnvelope,
        acknowledgment: Acknowledgment
    ) {
        when(retryProcessor.process(envelope = envelope)) {
            RetryProcessingResult.Processed ->
                acknowledgment.acknowledge()

            is RetryProcessingResult.RetryScheduled ->
                acknowledgment.acknowledge()
            is RetryProcessingResult.DeadLettered ->
                acknowledgment.acknowledge()
            is RetryProcessingResult.NotReady ->
                handleNotReady(envelope)
        }
    }

    private fun handleNotReady(envelope: RetryEnvelope) {
        TODO("Not yet implemented")
    }
}