package com.krizaldis.homepulse.state.retry

import java.time.Duration
import kotlin.random.Random

class JitterStrategy(
    private val config: RetryPolicyConfig,
    private val random: Random = Random.Default
) {

    fun apply(
        baseDelay: Duration
    ): Duration {

        if (config.jitterFactor <= 0.0) {
            return baseDelay
        }

        val factor =
            1.0 + random.nextDouble(
                -config.jitterFactor,
                config.jitterFactor
            )

        val jitteredMillis =
            (
                baseDelay.toMillis() * factor
            ).toLong()

        return Duration.ofMillis(
            jitteredMillis
                .coerceAtLeast(0)
                .coerceAtMost(
                    config.maxDelay.toMillis()
                )
        )
    }
}