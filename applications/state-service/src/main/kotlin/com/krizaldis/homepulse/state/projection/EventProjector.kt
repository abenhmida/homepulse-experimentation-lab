package com.krizaldis.homepulse.state.projection

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.state.domain.ProjectionCommand

/**
 * Converts one event type into a state projection.
 *
 * Projectors contain event-specific business mapping. They do not know
 * about Kafka offsets or DynamoDB transactions.
 */
interface EventProjector {

    fun supports(eventType: String): Boolean

    fun project(event: DomainEvent<*>): ProjectionCommand
}
