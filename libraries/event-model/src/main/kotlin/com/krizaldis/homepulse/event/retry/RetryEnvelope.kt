package com.krizaldis.homepulse.event.retry

import com.krizaldis.homepulse.event.DomainEvent

/**
 * Transport-independent retry message contract.
 *
 * The envelope belongs to the event-model library because it is exchanged
 * between the application and messaging infrastructure. It must not depend
 * on state-service or Kafka implementation classes.
 */
data class RetryEnvelope(
    val retry: RetryMetadata,
    val event: DomainEvent<*>
)
