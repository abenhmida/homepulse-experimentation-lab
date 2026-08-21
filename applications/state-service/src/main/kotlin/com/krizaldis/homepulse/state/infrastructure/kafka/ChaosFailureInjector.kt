package com.krizaldis.homepulse.state.infrastructure.kafka

import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Deliberately tiny fault-injection hook for the local reliability laboratory.
 * It is disabled by default and must never be enabled in production.
 */
@Component
class ChaosFailureInjector {

    private val crashBeforeAck =
        System.getenv("HOMEPULSE_CHAOS_CRASH_BEFORE_ACK")?.toBoolean() == true

    private val triggered = AtomicBoolean(false)

    fun beforeAcknowledgement() {
        if (crashBeforeAck && triggered.compareAndSet(false, true)) {
            throw SimulatedCrashException(
                "Chaos experiment: simulated crash before Kafka acknowledgement"
            )
        }
    }
}

class SimulatedCrashException(message: String) : RuntimeException(message)
