package com.krizaldis.homepulse.state

import com.krizaldis.homepulse.kafka.Environment
import com.krizaldis.homepulse.kafka.KafkaConsumerFactory
import com.krizaldis.homepulse.kafka.KafkaEventConsumer
import com.krizaldis.homepulse.kafka.KafkaProperties
import com.krizaldis.homepulse.state.observability.StateMetrics
import com.krizaldis.homepulse.state.infrastructure.dynamodb.DynamoDbConfig
import com.krizaldis.homepulse.state.infrastructure.dynamodb.DynamoDbStateRepository
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.Instant

@SpringBootApplication
class StateServiceApplication

fun main(args: Array<String>) {
    runApplication<StateServiceApplication>(*args)
}

@Configuration
class StateServiceConfig {

    @Bean
    fun dynamoDbClient(): DynamoDbClient {
        return DynamoDbConfig.client(
            endpoint = Environment.get("DYNAMODB_ENDPOINT", "http://localhost:8000"),
            region = Environment.get("AWS_REGION", "us-east-1")
        )
    }

    @Bean
    fun stateRepository(client: DynamoDbClient, registry: MeterRegistry): DynamoDbStateRepository {
        return DynamoDbStateRepository(
            client = client,
            tableName = Environment.get("DYNAMODB_TABLE", "homepulse"),
            registry = registry
        )
    }
}
