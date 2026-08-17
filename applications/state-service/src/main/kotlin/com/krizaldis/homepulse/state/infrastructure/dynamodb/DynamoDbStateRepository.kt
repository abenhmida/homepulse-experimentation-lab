package com.krizaldis.homepulse.state.infrastructure.dynamodb

import com.krizaldis.homepulse.event.DeviceTemperatureReported
import com.krizaldis.homepulse.state.observability.DynamoDbMetrics
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.Put
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException
import software.amazon.awssdk.services.dynamodb.model.Update
import java.time.Instant

class DynamoDbStateRepository(
    private val client: DynamoDbClient,
    private val tableName: String,
    private val registry: MeterRegistry
) {

    private val metrics = DynamoDbMetrics(registry)

    fun apply(
        event: DeviceTemperatureReported
    ): Boolean {
        val idempotencyKey = mapOf(
            "PK" to AttributeValue.builder()
                .s("IDEMPOTENCY#state")
                .build(),
            "SK" to AttributeValue.builder()
                .s("EVENT#${event.eventId}")
                .build()
        )

        val idempotencyItem = mapOf(
            "PK" to AttributeValue.builder()
                .s("IDEMPOTENCY#state")
                .build(),

            "SK" to AttributeValue.builder()
                .s("EVENT#${event.eventId}")
                .build(),

            "entityType" to AttributeValue.builder()
                .s("IDEMPOTENCY")
                .build(),

            "eventId" to AttributeValue.builder()
                .s(event.eventId)
                .build(),

            "deviceId" to AttributeValue.builder()
                .s(event.deviceId)
                .build(),

            "processedAt" to AttributeValue.builder()
                .s(Instant.now().toString())
                .build()
        )

        val deviceKey = mapOf(
            "PK" to AttributeValue.builder()
                .s("HOME#${event.homeId}")
                .build(),

            "SK" to AttributeValue.builder()
                .s("DEVICE#${event.deviceId}")
                .build()
        )

        val update = Update.builder()
            .tableName(tableName)
            .key(deviceKey)
            .updateExpression(
                """
                SET temperature = :temperature,
                    lastEventId = :eventId,
                    lastSequenceNumber = :sequence,
                    lastEventAt = :eventAt,
                    updatedAt = :updatedAt,
                    entityType = :entityType
                """.trimIndent()
            )
            .conditionExpression(
                """
                attribute_not_exists(lastSequenceNumber)
                OR lastSequenceNumber < :sequence
                """.trimIndent()
            )
            .expressionAttributeValues(
                mapOf(
                    ":temperature" to AttributeValue.builder()
                        .n(event.temperature.toString())
                        .build(),

                    ":eventId" to AttributeValue.builder()
                        .s(event.eventId)
                        .build(),

                    ":sequence" to AttributeValue.builder()
                        .n(event.sequenceNumber.toString())
                        .build(),

                    ":eventAt" to AttributeValue.builder()
                        .s(event.occurredAt)
                        .build(),

                    ":updatedAt" to AttributeValue.builder()
                        .s(Instant.now().toString())
                        .build(),

                    ":entityType" to AttributeValue.builder()
                        .s("DEVICE")
                        .build()
                )
            )
            .build()

        val transaction = TransactWriteItemsRequest.builder()
            .transactItems(
                listOf(
                    TransactWriteItem.builder()
                        .put(
                            Put.builder()
                                .tableName(tableName)
                                .item(idempotencyItem)
                                .conditionExpression(
                                    "attribute_not_exists(PK)"
                                )
                                .build()
                        )
                        .build(),

                    TransactWriteItem.builder()
                        .update(update)
                        .build()
                )
            ).build()

        val sample = Timer.start(registry)
        return try {
            metrics.transactions.increment()
            client.transactWriteItems(transaction)
            metrics.successfulRequests.increment()
            true
        } catch (_: TransactionCanceledException) {
            metrics.failures.increment()
            false
        } finally {
            sample.stop(metrics.transactionLatency)
        }
    }
}