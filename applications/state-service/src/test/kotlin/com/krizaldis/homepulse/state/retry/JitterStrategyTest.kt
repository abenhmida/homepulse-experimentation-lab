package com.krizaldis.homepulse.state.retry

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.random.Random

class JitterStrategyTest {

    @Test
    fun `zero jitter should preserve base delay`() {

        val config =
            RetryPolicyConfig(
                jitterFactor = 0.0
            )

        val strategy =
            JitterStrategy(
                config = config,
                random = Random(42)
            )

        val result =
            strategy.apply(
                Duration.ofSeconds(10)
            )

        assertEquals(
            Duration.ofSeconds(10),
            result
        )
    }
}