package com.krizaldis.homepulse.state.retry

import com.krizaldis.homepulse.observability.KafkaPropagation
import com.krizaldis.homepulse.observability.Telemetry
import com.krizaldis.homepulse.observability.Tracing
import com.krizaldis.homepulse.state.StateConfig
import io.opentelemetry.context.Context
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.consumer.ConsumerRecord

class RetryPublisher(
    private val config: StateConfig, private val telemetry: Telemetry
) : AutoCloseable {

    private val producer = KafkaProducer<String, ByteArray>(
        mapOf(
            "bootstrap.servers" to config.bootstrapServers,
            "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
            "value.serializer" to "org.apache.kafka.common.serialization.ByteArraySerializer",
            "acks" to "all",
            "enable.idempotence" to "true"
        )
    )

    fun publish(source: ConsumerRecord<String, ByteArray>, targetTopic: String, reason: Throwable) {
        val record = ProducerRecord(targetTopic, source.key(), source.value())
        record.headers().add("x-original-topic", source.topic().toByteArray())
        record.headers().add("x-original-partition", source.partition().toString().toByteArray())
        record.headers().add("x-original-offset", source.offset().toString().toByteArray())
        record.headers().add("x-failure-type", reason::class.java.name.toByteArray())

        val span = telemetry.tracer.spanBuilder(
            if (targetTopic == config.dlqTopic) {
                "dlq.publish"
            } else {
                "retry.publish"
            }
        ).startSpan()

        try {
            span.makeCurrent().use {
                span.setAttribute("messaging.system", "kafka")
                span.setAttribute("messaging.destination.name", targetTopic)
                span.setAttribute("failure.type", reason::class.java.simpleName)

                KafkaPropagation.inject(Context.current(), record)
                producer.send(record).get()

                if (targetTopic == config.dlqTopic) {
                    telemetry.eventsDlq.add(1)
                } else {
                    telemetry.eventsRetried.add(1)
                }
            }
        } catch (e: Exception) {
            Tracing.recordFailure(span, e)
            throw e
        } finally {
            span.end()
        }
    }

    override fun close() {
        producer.close()
    }
}