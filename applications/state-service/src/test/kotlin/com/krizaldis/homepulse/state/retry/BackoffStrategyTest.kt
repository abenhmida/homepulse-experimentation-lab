package com.krizaldis.homepulse.state.retry

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration

class BackoffStrategyTest {
    private val config = RetryPolicyConfig(
        maxAttempts = 10,
        initialDelay = Duration.ofSeconds(1),
        maxDelay = Duration.ofSeconds(60),
        multiplier = 2.0,
        jitterFactor = 0.0
    )

    private val sut = BackoffStrategy(config)

    @Test
    fun `attempt one should use initial delay`() {

        assertEquals(
            Duration.ofSeconds(1),
            sut.calculate(1)
        )
    }

    @Test
    fun `attempt two should double delay`() {

        assertEquals(
            Duration.ofSeconds(2),
            sut.calculate(2)
        )
    }

    @Test
    fun `attempt three should calculate four seconds`() {

        assertEquals(
            Duration.ofSeconds(4),
            sut.calculate(3)
        )
    }

    @Test
    fun `delay should be capped`() {

        assertEquals(
            Duration.ofSeconds(60),
            sut.calculate(10)
        )
    }
}