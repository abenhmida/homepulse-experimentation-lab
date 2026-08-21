package com.krizaldis.homepulse.state.retry

class RetryPolicy(
    private val config: RetryPolicyConfig,
    private val backoffStrategy: BackoffStrategy = BackoffStrategy(config),
    private val jitterStrategy: JitterStrategy = JitterStrategy(config)
) {

    fun evaluate(
        attempt: Int,
    ): RetryDecision {

        require(attempt >= 1) {
            "Retry attempt must be >= 1"
        }

        if (attempt > config.maxAttempts) {
            return RetryDecision.DeadLetter(
                attempt = attempt
            )
        }

        val baseDelay = backoffStrategy.calculate(attempt)

        val delay = jitterStrategy.apply(baseDelay)

        return RetryDecision.Retry(
            attempt = attempt,
            delay = delay
        )
    }
}
