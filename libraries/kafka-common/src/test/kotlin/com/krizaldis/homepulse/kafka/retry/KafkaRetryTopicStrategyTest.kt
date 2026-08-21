package com.krizaldis.homepulse.kafka.retry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KafkaRetryTopicStrategyTest {

    private val strategy = KafkaRetryTopicStrategy()

    @Test
    fun `should generate retry topic`() {
        assertEquals(
            "home.events.retry.3",
            strategy.retryTopic("home.events", 3)
        )
    }

    @Test
    fun `should generate dlq topic`() {
        assertEquals(
            "home.events.dlq",
            strategy.dlqTopic("home.events")
        )
    }
}
