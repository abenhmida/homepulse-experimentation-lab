package com.krizaldis.homepulse.state.config

import com.krizaldis.homepulse.state.retry.BackoffStrategy
import com.krizaldis.homepulse.state.retry.JitterStrategy
import com.krizaldis.homepulse.state.retry.RetryPolicy
import com.krizaldis.homepulse.state.retry.RetryPolicyConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableConfigurationProperties(
    RetryPolicyProperties::class
)
class RetryConfiguration {

    @Bean
    fun retryPolicyConfig(
        properties: RetryPolicyProperties
    ): RetryPolicyConfig =
        RetryPolicyConfig(
            maxAttempts = properties.maxAttempts,
            initialDelay = properties.initialDelay,
            maxDelay = properties.maxDelay,
            multiplier = properties.multiplier,
            jitterFactor = properties.jitterFactor
        )

    @Bean
    fun backoffStrategy(
        config: RetryPolicyConfig
    ): BackoffStrategy =
        BackoffStrategy(config)

    @Bean
    fun jitterStrategy(
        config: RetryPolicyConfig
    ): JitterStrategy =
        JitterStrategy(config)

    @Bean
    fun retryPolicy(
        config: RetryPolicyConfig,
        backoffStrategy: BackoffStrategy,
        jitterStrategy: JitterStrategy
    ): RetryPolicy =
        RetryPolicy(
            config = config,
            backoffStrategy = backoffStrategy,
            jitterStrategy = jitterStrategy
        )
}

@ConfigurationProperties(prefix = "homepulse.retry")
data class RetryPolicyProperties(
    var maxAttempts: Int = 5,
    var initialDelay: Duration = Duration.ofSeconds(1),
    var maxDelay: Duration = Duration.ofSeconds(30),
    var multiplier: Double = 2.0,
    var jitterFactor: Double = 0.20
)