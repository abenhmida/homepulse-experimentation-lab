package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.event.retry.FailureType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import java.time.Duration

class RetryPolicyTest {

    private val config = RetryPolicyConfig(
        maxAttempts = 3,
        initialDelay = Duration.ofSeconds(1),
        maxDelay = Duration.ofSeconds(60),
        multiplier = 2.0,
        jitterFactor = 0.0
    )

    private val policy = RetryPolicy(config)

    @Test
    fun `retryable failure schedules next attempt`() {
        val result = policy.decide(1, FailureType.RETRYABLE)

        val retry = assertInstanceOf(RetryDecision.Retry::class.java, result)

        assertEquals(2, retry.attempt)
        assertEquals(Duration.ofSeconds(1), retry.delay)
    }

    @Test
    fun `backoff increases with attempt`() {
        val result = policy.decide(2, FailureType.RETRYABLE)

        val retry = assertInstanceOf(RetryDecision.Retry::class.java, result)

        assertEquals(3, retry.attempt)
        assertEquals(Duration.ofSeconds(2), retry.delay)
    }

    @Test
    fun `maximum retryable attempt goes to dead letter`() {
        val result = policy.decide(3, FailureType.RETRYABLE)

        val deadLetter = assertInstanceOf(RetryDecision.DeadLetter::class.java, result)

        assertEquals(3, deadLetter.attempt)
    }

    @Test
    fun `permanent failure goes directly to dead letter`() {
        val result = policy.decide(1, FailureType.PERMANENT)

        assertInstanceOf(RetryDecision.DeadLetter::class.java, result)
    }

    @Test
    fun `duplicate is treated as terminal`() {
        val result = policy.decide(1, FailureType.DUPLICATE)

        assertInstanceOf(RetryDecision.DeadLetter::class.java, result)
    }

    @Test
    fun `retry delay never exceeds configured maximum`() {
        val result = policy.decide(2, FailureType.RETRYABLE)

        val retry = assertInstanceOf(RetryDecision.Retry::class.java, result)

        assert(retry.delay <= Duration.ofSeconds(60))
    }
}
