package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.event.retry.RetryEnvelope

interface RetryProcessor {
    fun process(envelope: RetryEnvelope): RetryProcessingResult
}