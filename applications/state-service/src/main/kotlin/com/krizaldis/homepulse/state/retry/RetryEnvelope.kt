package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.event.DomainEvent

data class RetryEnvelope(
    val retry: RetryMetadata,
    val event: DomainEvent<*>
)
