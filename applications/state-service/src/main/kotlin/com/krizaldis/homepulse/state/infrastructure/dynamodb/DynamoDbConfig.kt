package com.krizaldis.homepulse.state.infrastructure.dynamodb

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.net.URI

object DynamoDbConfig {
    fun client(
        endpoint: String,
        region: String
    ): DynamoDbClient {
        return DynamoDbClient.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        "dummy",
                        "dummy"
                    )
                )
            )
            .build()
    }
}