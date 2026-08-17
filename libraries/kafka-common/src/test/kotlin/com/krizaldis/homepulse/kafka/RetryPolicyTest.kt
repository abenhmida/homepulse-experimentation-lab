package com.krizaldis.homepulse.kafka

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RetryPolicyTest {

    @Test
    fun `transient failure should retry`() {
        val policy = RetryPolicy(
            maxAttempts = 3
        )

        val decision = policy.decide(
            currentAttempt = 1,
            failureType = FailureType.TRANSIENT
        )

        assertTrue(decision.retry)
    }

    @Test
    fun `maximum attempts should go to dlq`() {

        val policy = RetryPolicy(
            maxAttempts = 3
        )

        val decision =
            policy.decide(
                currentAttempt = 3,
                failureType = FailureType.TRANSIENT
            )

        assertFalse(decision.retry)
    }
}