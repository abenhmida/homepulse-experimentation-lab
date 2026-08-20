package com.krizaldis.homepulse.state.domain

/**
 * Generic representation of a materialized state projection.
 *
 * Event-specific projectors translate domain events into this neutral
 * representation. The DynamoDB adapter is responsible for translating
 * ProjectionValue into AWS AttributeValue.
 */
data class ProjectionCommand(
    val projectionType: String,
    val partitionKey: String,
    val sortKey: String,
    val sequenceNumber: Long,
    val attributes: Map<String, ProjectionValue>
)

sealed interface ProjectionValue {
    data class StringValue(val value: String) : ProjectionValue
    data class NumberValue(val value: Number) : ProjectionValue
    data class BooleanValue(val value: Boolean) : ProjectionValue
}
