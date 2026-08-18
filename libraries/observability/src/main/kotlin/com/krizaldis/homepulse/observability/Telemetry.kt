package com.krizaldis.homepulse.observability

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import java.lang.AutoCloseable
import java.time.Duration
import java.util.concurrent.TimeUnit

data class TelemetryConfig(
    val serviceName: String,
    val serviceVersion: String = "1.0.0",
    val environment: String = System.getenv("DEPLOYMENT_ENVIRONMENT") ?: "local",
    val oltpEndpoint: String = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") ?: "http://localhost:4317",
)

class Telemetry private constructor(
    val openTelemetry: OpenTelemetry,
    val tracer: Tracer,
    val meter: Meter,
    val eventsProcessed: LongCounter,
    val eventsFailed: LongCounter,
    val eventsRetried: LongCounter,
    val eventsDlq: LongCounter
) {
    companion object {
        fun start(config: TelemetryConfig): Telemetry {
            val resource = Resource.getDefault().merge(
                Resource.create(
                    Attributes.builder()
                        .put("service.name", config.serviceName)
                        .put("service.version", config.serviceVersion)
                        .put("deployment.environment", config.environment)
                        .build()
                )
            )
            val spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(config.oltpEndpoint)
                .setTimeout(5, TimeUnit.SECONDS)
                .build()

            val tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(
                    BatchSpanProcessor.builder(spanExporter)
                        .setScheduleDelay(Duration.ofMillis(200))
                        .build()
                )
                .build()

            val metricExporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint(config.oltpEndpoint)
                .setTimeout(5, TimeUnit.SECONDS)
                .build()

            val meterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(
                    PeriodicMetricReader.builder(metricExporter)
                        .setInterval(Duration.ofSeconds(10))
                        .build()
                )
                .build()

            val otel = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setMeterProvider(meterProvider)
                .buildAndRegisterGlobal()

            val tracer = otel.getTracer(config.serviceName, config.serviceVersion)
            val meter = otel.getMeter(config.serviceName)

            return Telemetry(
                openTelemetry = otel,
                tracer = tracer,
                meter = meter,
                eventsProcessed = meter.counterBuilder("homepulse.events.processed")
                    .setDescription("Successfully processed events")
                    .build(),
                eventsFailed = meter.counterBuilder("homepulse.events.failed")
                    .setDescription("Failed event processing attempts")
                    .build(),
                eventsRetried = meter.counterBuilder("homepulse.events.retried")
                    .setDescription("Events sent to retry topics")
                    .build(),
                eventsDlq = meter.counterBuilder("homepulse.events.dlq")
                    .setDescription("Events sent to the DLQ")
                    .build()
            )
        }
    }
}