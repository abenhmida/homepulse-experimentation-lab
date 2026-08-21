package com.krizaldis.homepulse.state.retry

import java.time.Duration
import kotlin.math.pow

class BackoffStrategy(
    private val config: RetryPolicyConfig
) {
    fun calculate(attempt: Int): Duration {

        require(attempt >= 1) {
            "Retry attempt must >= 1"
        }

        val multiplier = config.multiplier.pow((attempt - 1).toDouble())

        val delayMillis = config.initialDelay
            .toMillis()
            .toDouble()
            .times(multiplier)
            .toLong()

        return Duration.ofMillis(
            delayMillis.coerceAtMost(
                config.maxDelay.toMillis()
            )
        )
    }
}