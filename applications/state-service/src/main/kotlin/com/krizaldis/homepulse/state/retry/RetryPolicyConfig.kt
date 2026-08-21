package com.krizaldis.homepulse.state.retry

import java.time.Duration

data class RetryPolicyConfig(
    val maxAttempts: Int = 5,
    val initialDelay: Duration = Duration.ofSeconds(1),
    val maxDelay: Duration = Duration.ofSeconds(60),
    val multiplier: Double = 2.0,
    val jitterFactor: Double = 0.20
)
