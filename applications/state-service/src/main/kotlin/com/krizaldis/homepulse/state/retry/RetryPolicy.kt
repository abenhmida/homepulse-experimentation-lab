package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.event.retry.FailureType

class RetryPolicy(
    private val config: RetryPolicyConfig,
    private val backoffStrategy: BackoffStrategy = BackoffStrategy(config),
    private val jitterStrategy: JitterStrategy = JitterStrategy(config)
) {

    fun initialDecision(failureType: FailureType): RetryDecision =
        if (failureType == FailureType.RETRYABLE && config.maxAttempts > 0) {
            RetryDecision.Retry(
                attempt = 1,
                delay = jitterStrategy.apply(backoffStrategy.calculate(1))
            )
        } else {
            RetryDecision.DeadLetter(attempt = 0)
        }

    fun decide(
        currentAttempt: Int,
        failureType: FailureType
    ): RetryDecision {
        require(currentAttempt >= 1) {
            "Retry attempt must be >= 1"
        }

        if (failureType != FailureType.RETRYABLE || currentAttempt >= config.maxAttempts) {
            return RetryDecision.DeadLetter(currentAttempt)
        }

        return RetryDecision.Retry(
            attempt = currentAttempt + 1,
            delay = jitterStrategy.apply(backoffStrategy.calculate(currentAttempt))
        )
    }
}
