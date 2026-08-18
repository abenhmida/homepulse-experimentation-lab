package com.krizaldis.homepulse.state.infrastructure.dynamodb

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.TemperatureReading
import com.krizaldis.homepulse.state.StateConfig
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.net.URI
import java.time.Instant

class DynamoDbStateRepository(
    val config: StateConfig
) : AutoCloseable {

    private val client = buildClient(config)

    fun tryClaimEvent(event: DomainEvent<*>): Boolean {
        return try {
            client.putItem(
                PutItemRequest.builder()
                    .tableName(config.idempotencyTable)
                    .item(
                        mapOf(
                            "eventId" to AttributeValue.builder().s(event.metadata.eventId).build(),
                            "deviceId" to AttributeValue.builder().s(event.metadata.deviceId).build(),
                            "claimedAt" to AttributeValue.builder().s(Instant.now().toString()).build()
                        )
                    )
                    .conditionExpression("attribute_not_exists(eventId)")
                    .build()
            )
            true
        } catch (_: ConditionalCheckFailedException) {
            false
        }
    }

    fun projectTemperature(event: DomainEvent<TemperatureReading>) {
        val payload = event.payload

        client.putItem(
            PutItemRequest.builder()
                .tableName(config.stateTable)
                .item(
                    mapOf(
                        "deviceId" to AttributeValue.builder().s(event.metadata.deviceId).build(),
                        "eventType" to AttributeValue.builder().s(event.metadata.eventType).build(),
                        "eventId" to AttributeValue.builder().s(event.metadata.eventId).build(),
                        "sequenceNumber" to AttributeValue.builder().n(event.metadata.sequenceNumber.toString())
                            .build(),
                        "temperatureCelsius" to AttributeValue.builder().n(payload.temperatureCelsius.toString())
                            .build(),
                        "humidityPercent" to AttributeValue.builder().n(payload.humidityPercent.toString()).build(),
                        "occurredAt" to AttributeValue.builder().s(event.metadata.occurredAt.toString()).build(),
                        "updatedAt" to AttributeValue.builder().s(Instant.now().toString()).build()
                    )
                )
                .build()
        )
    }

    private fun buildClient(config: StateConfig): DynamoDbClient {
        val builder = DynamoDbClient.builder()
            .region(Region.of(config.awsRegion))

        if (config.dynamoEndpoint != null) {
            builder
                .endpointOverride(URI.create(config.dynamoEndpoint))
                .credentialsProvider(AnonymousCredentialsProvider.create())
        }

        return builder.build()
    }

    override fun close() {
        client.close()
    }
}