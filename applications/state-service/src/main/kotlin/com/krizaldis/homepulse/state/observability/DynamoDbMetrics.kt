package com.krizaldis.homepulse.state.observability

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer

class DynamoDbMetrics(
    registry: MeterRegistry
) {
    val requests =
        Counter.builder("homepulse_dynamodb_requests_total")
            .register(registry)

    val successfulRequests =
        Counter.builder("homepulse_dynamodb_success_total")
            .register(registry)

    val failures =
        Counter.builder("homepulse_dynamodb_failures_total")
            .register(registry)

    val conditionalFailures =
        Counter.builder("homepulse_dynamodb_conditional_failures_total")
            .register(registry)

    val throttled =
        Counter.builder("homepulse_dynamodb_throttled_total")
            .register(registry)

    val transactions =
        Counter.builder("homepulse_dynamodb_transactions_total")
            .register(registry)

    val transactionLatency =
        Timer.builder("homepulse_dynamodb_transaction_duration")
            .publishPercentiles(
                0.5,
                0.95,
                0.99
            )
            .register(registry)
}