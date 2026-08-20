package com.krizaldis.homepulse.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import java.lang.AutoCloseable
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

class KafkaEventConsumer(
    private val consumer: KafkaConsumer<String, ByteArray>,
    private val topics: Collection<String>,
    private val handler: (ConsumerRecord<String, ByteArray>) -> Unit
): AutoCloseable {
    private val running = AtomicBoolean(true)

    fun start() {
        consumer.subscribe(topics)
        try {
            while (running.get()) {
                val records = consumer.poll(Duration.ofMillis(500))
                for (record in records) {
                    handler(record)
                }

                if (!records.isEmpty) {
                    consumer.commitSync()
                }
            }
        } catch (e: WakeupException) {
            if (running.get()) {
                throw e
            }
        } finally {
            consumer.close()
        }
    }

    fun stop() {
        running.set(false)
        consumer.wakeup()
    }

    override fun close() {
        stop()
    }
}