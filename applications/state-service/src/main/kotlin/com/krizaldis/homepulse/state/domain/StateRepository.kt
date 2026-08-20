package com.krizaldis.homepulse.state.domain

import com.krizaldis.homepulse.event.DomainEvent

/**
 * Persistence boundary for materialized state.
 *
 * The repository knows about event identity/versioning and projection
 * persistence, but it does not know about individual event payload types.
 */
interface StateRepository {

    fun apply(
        event: DomainEvent<*>,
        projection: ProjectionCommand
    ): ProjectionResult
}
