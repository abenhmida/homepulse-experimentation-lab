package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.event.retry.RetryEnvelope
import java.util.concurrent.CompletionStage

interface RetryPublisher {
    fun publish(
        envelope: RetryEnvelope
    ): CompletionStage<PublishResult>
}