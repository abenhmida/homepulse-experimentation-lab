package com.krizaldis.homepulse.state.retry

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration

class RetryPolicyTest {
    private val config =
        RetryPolicyConfig(
            maxAttempts = 5,
            initialDelay = Duration.ofSeconds(1),
            maxDelay = Duration.ofSeconds(60),
            multiplier = 2.0,
            jitterFactor = 0.0
        )

    private val policy =
        RetryPolicy(config)

    @Test
    fun `attempt within limit should retry`() {

        val result =
            policy.evaluate(3)

        assertInstanceOf(
            RetryDecision.Retry::class.java, result
        )

        val retry =
            result as RetryDecision.Retry

        assertEquals(
            3,
            retry.attempt
        )

        assertEquals(
            Duration.ofSeconds(4),
            retry.delay
        )
    }

    @Test
    fun `attempt above limit should go to dead letter`() {

        val result =
            policy.evaluate(6)

        assertInstanceOf(
            RetryDecision.DeadLetter::class.java, result
        )


        val deadLetter =
            result as RetryDecision.DeadLetter

        assertEquals(
            6,
            deadLetter.attempt
        )
    }

    @Test
    fun `retry delay should never exceed configured maximum`() {

        val result =
            policy.evaluate(5)

        assertInstanceOf(
            RetryDecision.Retry::class.java, result
        )

        val retry =
            result as RetryDecision.Retry

        assert(
            retry.delay <= Duration.ofSeconds(60)
        )
    }
}