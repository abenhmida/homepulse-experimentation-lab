package com.krizaldis.homepulse.observability

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.context.propagation.TextMapSetter
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import java.nio.charset.StandardCharsets

object KafkaPropagation {
    private val propagator = GlobalOpenTelemetry.getPropagators().textMapPropagator

    fun inject(
        context: Context,
        record: ProducerRecord<String, ByteArray>
    ) {
        propagator.inject(context, record, ProducerSetter)
    }

    fun extract(
        record: ConsumerRecord<String, ByteArray>
    ): Context =
        propagator.extract(Context.current(), record, ConsumerGetter)

    private object ProducerSetter : TextMapSetter<ProducerRecord<String, ByteArray>> {
        override fun set(
            carrier: ProducerRecord<String, ByteArray>?,
            key: String,
            value: String
        ) {
            carrier ?: return
            carrier.headers().remove(key)
            carrier.headers().add(key, value.toByteArray(StandardCharsets.UTF_8))
        }
    }

    private object ConsumerGetter : TextMapGetter<ConsumerRecord<String, ByteArray>> {
        override fun keys(carrier: ConsumerRecord<String, ByteArray>): Iterable<String?>? {
            return carrier.headers()?.map { it.key() } ?: emptyList()
        }

        override fun get(
            carrier: ConsumerRecord<String, ByteArray>?,
            key: String
        ): String? =
            carrier?.headers()?.lastHeader(key)?.value()
                ?.toString(StandardCharsets.UTF_8)
    }
}