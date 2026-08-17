package com.krizaldis.homepulse.state.observability

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

class StateMetrics(
    registry: MeterRegistry
) {
    val eventsReceived =
        Counter.builder("homepulse_events_received_total")
            .description("Number of events received from Kafka")
            .register(registry)

    val eventsApplied =
        Counter.builder("homepulse_projection_applied_total")
            .description("Number of events successfully projected")
            .register(registry)

    val duplicates =
        Counter.builder("homepulse_projection_duplicate_total")
            .description("Number of duplicate events")
            .register(registry)

    val stale =
        Counter.builder("homepulse_projection_stale_total")
            .description("Number of stale events")
            .register(registry)

    val failures =
        Counter.builder("homepulse_projection_failed_total")
            .description("Number of projection failures")
            .register(registry)

    val projectionLatency =
        Timer.builder("homepulse_projection_duration")
            .description("Projection processing duration")
            .publishPercentiles(
                0.5,
                0.95,
                0.99
            )
            .register(registry)
}