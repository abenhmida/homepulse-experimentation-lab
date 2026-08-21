package com.krizaldis.homepulse.state.application

import com.krizaldis.homepulse.event.retry.FailureType
import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.state.domain.ProcessingResult
import com.krizaldis.homepulse.state.retry.RetryDecision
import com.krizaldis.homepulse.state.retry.RetryEnvelopeFactory
import com.krizaldis.homepulse.state.retry.RetryMessagePublisher
import com.krizaldis.homepulse.state.retry.RetryPolicy
import com.krizaldis.homepulse.state.retry.RetryProcessingResult
import com.krizaldis.homepulse.state.retry.RetryProcessor
import java.time.Clock

/**
 * Application orchestration for retry processing.
 *
 * It delegates actual event processing to StateService. Kafka concerns remain
 * in RetryKafkaListener and RetryMessagePublisher.
 */
class RetryProcessingService(
    private val stateService: StateService,
    private val retryPolicy: RetryPolicy,
    private val retryEnvelopeFactory: RetryEnvelopeFactory,
    private val publisher: RetryMessagePublisher,
    private val clock: Clock = Clock.systemUTC()
) : RetryProcessor {

    override fun process(envelope: RetryEnvelope): RetryProcessingResult {
        val now = clock.instant()

        if (envelope.retry.nextAttemptAt.isAfter(now)) {
            return RetryProcessingResult.NotReady(envelope.retry.nextAttemptAt)
        }

        return when (val result = stateService.process(envelope.event)) {
            ProcessingResult.Applied,
            ProcessingResult.Duplicate,
            ProcessingResult.Stale -> RetryProcessingResult.Processed

            is ProcessingResult.RetryableFailure ->
                handleFailure(
                    envelope = envelope,
                    failureType = FailureType.RETRYABLE,
                    exception = result.cause,
                    now = now
                )

            is ProcessingResult.PermanentFailure ->
                handleFailure(
                    envelope = envelope,
                    failureType = FailureType.PERMANENT,
                    exception = result.cause,
                    now = now
                )
        }
    }

    private fun handleFailure(
        envelope: RetryEnvelope,
        failureType: FailureType,
        exception: Throwable,
        now: java.time.Instant
    ): RetryProcessingResult {
        return when (
            val decision = retryPolicy.decide(
                currentAttempt = envelope.retry.attempt,
                failureType = failureType
            )
        ) {
            is RetryDecision.Retry -> {
                val retryEnvelope = retryEnvelopeFactory.nextAttempt(
                    current = envelope,
                    failure = failureType,
                    exception = exception,
                    nextAttempt = decision.attempt,
                    nextAttemptAt = now.plus(decision.delay),
                    now = now
                )

                val publication = publisher.publishRetry(retryEnvelope)

                RetryProcessingResult.RetryScheduled(publication)
            }

            is RetryDecision.DeadLetter -> {
                val deadLetter = retryEnvelopeFactory.deadLetter(
                    current = envelope,
                    failure = failureType,
                    exception = exception,
                    now = now
                )

                val publication = publisher.publishDeadLetter(deadLetter)

                RetryProcessingResult.DeadLettered(publication)
            }
        }
    }
}
