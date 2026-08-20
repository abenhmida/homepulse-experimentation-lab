package com.krizaldis.homepulse.state.projection

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.state.domain.ProjectionCommand
import com.krizaldis.homepulse.state.domain.UnsupportedEventException
import org.springframework.stereotype.Component

/**
 * Selects the projector responsible for the incoming event type.
 */
@Component
class ProjectionDispatcher(
    private val projectors: List<EventProjector>
) {

    fun dispatch(event: DomainEvent<*>): ProjectionCommand {

        val projector = projectors.firstOrNull {
            it.supports(event.metadata.eventType)
        } ?: throw UnsupportedEventException(
            "Unsupported event type: ${event.metadata.eventType}"
        )

        return projector.project(event)
    }
}
