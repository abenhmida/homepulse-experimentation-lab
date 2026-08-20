package com.krizaldis.homepulse.state

import com.krizaldis.homepulse.observability.Telemetry
import com.krizaldis.homepulse.observability.TelemetryConfig
import com.krizaldis.homepulse.state.domain.FailureClassifier
import com.krizaldis.homepulse.state.infrastructure.dynamodb.DynamoDbStateRepository
import com.krizaldis.homepulse.state.retry.RetryPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StateInfrastructureConfiguration {

    @Bean
    fun stateConfig(): StateConfig = StateConfig()

    @Bean(destroyMethod = "close")
    fun telemetry(): Telemetry =
        Telemetry.start(
            TelemetryConfig(
                serviceName = "homepulse-state-service",
                serviceVersion = "0.1.0-SNAPSHOT"
            )
        )

    @Bean
    fun failureClassifier(): FailureClassifier =
        FailureClassifier()

    @Bean(destroyMethod = "close")
    fun stateRepository(config: StateConfig): DynamoDbStateRepository =
        DynamoDbStateRepository(config)

    @Bean(destroyMethod = "close")
    fun retryPublisher(config: StateConfig, telemetry: Telemetry): RetryPublisher =
        RetryPublisher(config, telemetry)
}
