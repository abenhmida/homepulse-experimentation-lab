package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.state.failure.FailureType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RetryPolicyTest {

    @Test
    fun `transient failure should retry`() {
        val policy = RetryPolicy(
            maxAttempts = 3
        )

        val decision = policy.decide(
            currentAttempt = 1,
            failureType = FailureType.RETRYABLE
        )

        assertTrue(decision.shouldRetry)
    }

    @Test
    fun `maximum attempts should go to dlq`() {

        val policy = RetryPolicy(
            maxAttempts = 3
        )

        val decision =
            policy.decide(
                currentAttempt = 3,
                failureType = FailureType.RETRYABLE
            )

        assertFalse(decision.shouldRetry)
    }
}