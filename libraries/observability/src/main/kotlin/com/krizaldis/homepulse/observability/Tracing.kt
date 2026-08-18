package com.krizaldis.homepulse.observability

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer

object Tracing {
    lateinit var tracer: Tracer

    fun initialize() {
        tracer = GlobalOpenTelemetry.getTracer("homepulse")
    }

    fun recordFailure(span: Span, error: Throwable) {
        span.recordException(error)
        span.setStatus(StatusCode.ERROR)
    }

    fun consumerSpan(
        telemetry: Telemetry,
        recordTopic: String,
        partition: Int,
        offset: Long
    ): Span {
        return telemetry.tracer.spanBuilder("kafka.consume")
            .setSpanKind(SpanKind.CONSUMER)
            .setAttribute("messaging.system", "kafka")
            .setAttribute("messaging.destination.name", recordTopic)
            .setAttribute("messaging.kafka.partition", partition.toLong())
            .setAttribute("messaging.kafka.offset", offset)
            .startSpan()
    }

    fun producerSpan(
        telemetry: Telemetry,
        topic: String
    ): Span =
        telemetry.tracer.spanBuilder("kafka.publish")
            .setSpanKind(SpanKind.PRODUCER)
            .setAttribute("messaging.system", "kafka")
            .setAttribute("messaging.destination.name", topic)
            .startSpan()
}