package com.krizaldis.homepulse.infrastructure.dynamodb

import com.krizaldis.homepulse.state.IdempotencyRepository
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import java.time.Instant

class DynamoDbIdempotencyRepository(
    private val client: DynamoDbClient,
    private val tableName: String
) : IdempotencyRepository {
    override suspend fun tryAcquire(eventId: String, deviceId: String): Boolean {
        val item = mapOf(
            "PK" to AttributeValue.builder()
                .s("IDEMPOTENCY#state")
                .build(),

            "SK" to AttributeValue.builder()
                .s("EVENT#$eventId")
                .build(),

            "entityType" to AttributeValue.builder()
                .s("IDEMPOTENCY")
                .build(),

            "eventId" to AttributeValue.builder()
                .s(eventId)
                .build(),

            "deviceId" to AttributeValue.builder()
                .s(deviceId)
                .build(),

            "processedAt" to AttributeValue.builder()
                .s(Instant.now().toString())
                .build()
        )

        val request = PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .conditionExpression("attribute_not_exists(PK)")
            .build()

        return try {
            client.putItem(request)
            true
        } catch (_: ConditionalCheckFailedException) {
            false
        }
    }
}