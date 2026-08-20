package com.krizaldis.homepulse.state.infrastructure.dynamodb

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.state.StateConfig
import com.krizaldis.homepulse.state.domain.ProjectionCommand
import com.krizaldis.homepulse.state.domain.ProjectionValue
import com.krizaldis.homepulse.state.domain.ProjectionResult
import com.krizaldis.homepulse.state.domain.StateRepository
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.Put
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException
import java.net.URI
import java.time.Instant

/**
 * DynamoDB adapter for generic materialized-state projections.
 *
 * The adapter is deliberately unaware of TemperatureReported, DoorStateChanged,
 * EnergyMeasured, etc. Event-specific mapping belongs to EventProjector.
 */
class DynamoDbStateRepository(
    private val config: StateConfig
) : AutoCloseable, StateRepository {

    private val client = buildClient(config)

    override fun apply(
        event: DomainEvent<*>,
        projection: ProjectionCommand
    ): ProjectionResult {

        if (idempotencyExists(event)) {
            return ProjectionResult.Duplicate
        }

        val currentSequence =
            getCurrentSequence(projection)

        if (
            currentSequence != null &&
            currentSequence >= projection.sequenceNumber
        ) {
            return ProjectionResult.Stale
        }

        val transaction =
            TransactWriteItemsRequest.builder()
                .transactItems(
                    idempotencyWrite(event),
                    projectionWrite(projection)
                )
                .build()

        return try {

            client.transactWriteItems(transaction)

            ProjectionResult.Applied

        } catch (exception: TransactionCanceledException) {

            /*
             * The transaction is the concurrency gate.
             *
             * If another consumer won the idempotency race,
             * this event is a duplicate. Otherwise the cancellation
             * is a real infrastructure/transaction failure and must
             * be classified by the application layer.
             */
            when {
                idempotencyExists(event) ->
                    ProjectionResult.Duplicate

                getCurrentSequence(projection)
                    ?.let { it >= projection.sequenceNumber } == true ->
                    ProjectionResult.Stale

                else ->
                    throw exception
            }
        }
    }

    private fun idempotencyWrite(
        event: DomainEvent<*>
    ): TransactWriteItem {

        val item = mapOf(
            "pk" to stringValue(
                "EVENT#${event.metadata.eventId}"
            ),
            "eventId" to stringValue(
                event.metadata.eventId
            ),
            "eventType" to stringValue(
                event.metadata.eventType
            ),
            "homeId" to stringValue(
                event.metadata.homeId
            ),
            "deviceId" to stringValue(
                event.metadata.deviceId
            ),
            "sequenceNumber" to numberValue(
                event.metadata.sequenceNumber
            ),
            "processedAt" to stringValue(
                Instant.now().toString()
            )
        )

        return TransactWriteItem.builder()
            .put(
                Put.builder()
                    .tableName(config.idempotencyTable)
                    .item(item)
                    .conditionExpression(
                        "attribute_not_exists(pk)"
                    )
                    .build()
            )
            .build()
    }

    private fun projectionWrite(
        projection: ProjectionCommand
    ): TransactWriteItem {

        val item = mutableMapOf<String, AttributeValue>()

        item["pk"] = stringValue(projection.partitionKey)
        item["sk"] = stringValue(projection.sortKey)

        projection.attributes.forEach { (name, value) ->
            item[name] = value.toAttributeValue()
        }

        /*
         * Sequence condition is evaluated against the same item
         * that is being projected. This prevents an older event from
         * overwriting newer state.
         */
        return TransactWriteItem.builder()
            .put(
                Put.builder()
                    .tableName(config.stateTable)
                    .item(item)
                    .conditionExpression(
                        "attribute_not_exists(sequenceNumber) " +
                            "OR sequenceNumber < :incomingSequence"
                    )
                    .expressionAttributeValues(
                        mapOf(
                            ":incomingSequence" to
                                numberValue(projection.sequenceNumber)
                        )
                    )
                    .build()
            )
            .build()
    }

    private fun idempotencyExists(
        event: DomainEvent<*>
    ): Boolean {

        val response =
            client.getItem { builder ->
                builder
                    .tableName(config.idempotencyTable)
                    .key(
                        mapOf(
                            "pk" to stringValue(
                                "EVENT#${event.metadata.eventId}"
                            )
                        )
                    )
                    .consistentRead(true)
            }

        return response.hasItem()
    }

    private fun getCurrentSequence(
        projection: ProjectionCommand
    ): Long? {

        val response =
            client.getItem { builder ->
                builder
                    .tableName(config.stateTable)
                    .key(
                        mapOf(
                            "pk" to stringValue(
                                projection.partitionKey
                            ),
                            "sk" to stringValue(
                                projection.sortKey
                            )
                        )
                    )
                    .projectionExpression("sequenceNumber")
                    .consistentRead(true)
            }

        return response.item()["sequenceNumber"]
            ?.n()
            ?.toLong()
    }

    private fun ProjectionValue.toAttributeValue(): AttributeValue =
        when (this) {
            is ProjectionValue.StringValue ->
                stringValue(value)

            is ProjectionValue.NumberValue ->
                numberValue(value)

            is ProjectionValue.BooleanValue ->
                AttributeValue.builder()
                    .bool(value)
                    .build()
        }

    private fun stringValue(value: String): AttributeValue =
        AttributeValue.builder()
            .s(value)
            .build()

    private fun numberValue(value: Number): AttributeValue =
        AttributeValue.builder()
            .n(value.toString())
            .build()

    private fun buildClient(
        config: StateConfig
    ): DynamoDbClient {

        val builder =
            DynamoDbClient.builder()
                .region(Region.of(config.awsRegion))

        if (config.dynamoEndpoint != null) {
            builder
                .endpointOverride(
                    URI.create(config.dynamoEndpoint)
                )
                .credentialsProvider(
                    AnonymousCredentialsProvider.create()
                )
        }

        return builder.build()
    }

    override fun close() {
        client.close()
    }
}
