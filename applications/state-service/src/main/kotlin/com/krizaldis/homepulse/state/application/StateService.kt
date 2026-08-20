package com.krizaldis.homepulse.state.application

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.TemperatureReading
import com.krizaldis.homepulse.kafka.JsonEventDeserializer
import com.krizaldis.homepulse.observability.KafkaPropagation
import com.krizaldis.homepulse.observability.Telemetry
import com.krizaldis.homepulse.observability.Tracing
import com.krizaldis.homepulse.state.StateConfig
import com.krizaldis.homepulse.state.infrastructure.dynamodb.DynamoDbStateRepository
import com.krizaldis.homepulse.state.retry.RetryPublisher
import io.opentelemetry.api.trace.Span
import io.opentelemetry.context.Scope
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

@Component
class StateService(
    private val config: StateConfig,
    private val telemetry: Telemetry,
    private val repository: DynamoDbStateRepository,
    private val retryPublisher: RetryPublisher
) : AutoCloseable {
    private val log = LoggerFactory.getLogger(javaClass)
    private val running = AtomicBoolean(true)
    private val consumer = KafkaConsumer<String, ByteArray>(
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to config.bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to config.groupId,
            ConsumerConfig.CLIENT_ID_CONFIG to "${config.groupId}-consumer",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to config.autoOffsetReset,
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to config.maxPollRecords,
            ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to config.maxPollIntervalMs
        )
    )

    fun run() {
        consumer.subscribe(listOf(config.inputTopic))
        log.info("State consumer started topic={} groupId={}", config.inputTopic, config.groupId)
        try {
            while (running.get()) {
                val records = consumer.poll(Duration.ofMillis(config.pollTimeoutMs))
                for (record in records) {
                    process(record)
                    consumer.commitSync()
                }
            }
        } finally {
            consumer.close()
            log.info("State consumer stopped")
        }
    }

    private fun process(record: ConsumerRecord<String, ByteArray>) {
        val parentContext = KafkaPropagation.extract(record)
        val span = Tracing.consumerSpan(telemetry, record.topic(), record.partition(), record.offset())
        var scope: Scope? = null
        try {
            scope = span.makeCurrent()
            val event = JsonEventDeserializer().deserialize(record.value(), TemperatureReading::class.java)
            span.setAttribute("event.id", event.metadata.eventId)
            span.setAttribute("event.type", event.metadata.eventType)
            span.setAttribute("event.schema_version", event.metadata.schemaVersion.toLong())
            span.setAttribute("device.id", event.metadata.deviceId)
            span.setAttribute("event.age.ms", Duration.between(event.metadata.occurredAt, Instant.now()).toMillis())

            val processingSpan = telemetry.tracer.spanBuilder("event.process").setParent(parentContext).startSpan()
            processingSpan.makeCurrent().use {
                if (!repository.tryClaimEvent(event)) {
                    span.setAttribute("event.result", "DUPLICATE")
                    log.info("Duplicate event ignored eventId={}", event.metadata.eventId)
                    return
                }
                repository.projectTemperature(event)
                telemetry.eventsProcessed.add(1)
                span.setAttribute("event.result", "APPLIED")
                log.info("Event projected eventId={} deviceId={} partition={} offset={}", event.metadata.eventId, event.metadata.deviceId, record.partition(), record.offset())
            }
            processingSpan.end()
        } catch (e: Exception) {
            telemetry.eventsFailed.add(1)
            Tracing.recordFailure(span, e)
            log.error("Event processing failed topic={} partition={} offset={}", record.topic(), record.partition(), record.offset(), e)
            retryPublisher.publish(record, config.retryTopic, e)
        } finally {
            scope?.close()
            span.end()
        }
    }

    override fun close() {
        running.set(false)
        consumer.wakeup()
        retryPublisher.close()
        repository.close()
    }
}
