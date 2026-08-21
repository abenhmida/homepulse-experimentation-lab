package com.krizaldis.homepulse.state.infrastructure.kafka

import com.krizaldis.homepulse.event.retry.FailureType
import com.krizaldis.homepulse.state.application.StateService
import com.krizaldis.homepulse.state.domain.ProcessingResult
import com.krizaldis.homepulse.state.retry.RetryEnvelopeFactory
import com.krizaldis.homepulse.state.retry.RetryMessagePublisher
import com.krizaldis.homepulse.state.retry.RetryPolicy
import com.krizaldis.homepulse.state.retry.RetryDecision
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Clock

/**
 * Main event consumer. It creates the first retry envelope when the original
 * event cannot be applied successfully.
 */
@Component
class EventKafkaListener(
    private val stateService: StateService,
    private val retryPolicy: RetryPolicy,
    private val retryEnvelopeFactory: RetryEnvelopeFactory,
    private val retryPublisher: RetryMessagePublisher,
    private val codec: EventJsonCodec,
    private val clock: Clock,
    private val chaosFailureInjector: ChaosFailureInjector
) {

    @KafkaListener(
        topics = ["\${homepulse.kafka.topics.events}"],
        groupId = "\${homepulse.kafka.consumer.events-group-id}"
    )
    fun consume(
        record: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment
    ) {
        val event = codec.deserializeEvent(record.value())
        val now = clock.instant()

        when (val result = stateService.process(event)) {
            ProcessingResult.Applied,
            ProcessingResult.Duplicate,
            ProcessingResult.Stale -> acknowledge(acknowledgment)

            is ProcessingResult.RetryableFailure ->
                handleFailure(
                    record = record,
                    event = event,
                    failure = FailureType.RETRYABLE,
                    exception = result.cause,
                    now = now,
                    acknowledgment = acknowledgment
                )

            is ProcessingResult.PermanentFailure ->
                handleFailure(
                    record = record,
                    event = event,
                    failure = FailureType.PERMANENT,
                    exception = result.cause,
                    now = now,
                    acknowledgment = acknowledgment
                )
        }
    }

    private fun acknowledge(acknowledgment: Acknowledgment) {
        chaosFailureInjector.beforeAcknowledgement()
        acknowledgment.acknowledge()
    }

    private fun handleFailure(
        record: ConsumerRecord<String, String>,
        event: com.krizaldis.homepulse.event.DomainEvent<*>,
        failure: FailureType,
        exception: Throwable,
        now: java.time.Instant,
        acknowledgment: Acknowledgment
    ) {
        when (val decision = retryPolicy.initialDecision(failure)) {
            is RetryDecision.Retry -> {
                val envelope = retryEnvelopeFactory.initialFailure(
                    event = event,
                    originalTopic = record.topic(),
                    originalPartition = record.partition(),
                    originalOffset = record.offset(),
                    failure = failure,
                    exception = exception,
                    attempt = decision.attempt,
                    nextAttemptAt = now.plus(decision.delay),
                    now = now
                )

                retryPublisher.publishRetry(envelope)
                acknowledge(acknowledgment)
            }

            is RetryDecision.DeadLetter -> {
                val envelope = retryEnvelopeFactory.initialFailure(
                    event = event,
                    originalTopic = record.topic(),
                    originalPartition = record.partition(),
                    originalOffset = record.offset(),
                    failure = failure,
                    exception = exception,
                    attempt = decision.attempt,
                    nextAttemptAt = now,
                    now = now
                )

                retryPublisher.publishDeadLetter(envelope)
                acknowledge(acknowledgment)
            }
        }
    }
}
