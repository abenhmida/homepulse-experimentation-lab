package com.krizaldis.homepulse.kafka

class ExponentialBackoff(
    private val initialDelayMs: Long = 1_000,
    private val maxDelayMs: Long = 30_000
) {
    fun delayFor(attempt: Int): Long {
        val delay = initialDelayMs * (1L shl (attempt - 1))

        return delay.coerceAtMost(maxDelayMs)
    }
}