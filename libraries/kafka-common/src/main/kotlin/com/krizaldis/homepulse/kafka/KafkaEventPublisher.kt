package com.krizaldis.homepulse.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.concurrent.ExecutionException

/**
 * Synchronous publishing adapter.
 *
 * EventPublisher deliberately returns only after Kafka has acknowledged the
 * record. This is required by the retry/acknowledgement contract: the caller
 * must not acknowledge the consumed event before the replacement message is
 * durably accepted by Kafka.
 */
class KafkaEventPublisher(
    private val producer: KafkaProducer<String, String>
) : EventPublisher {

    override fun publish(event: PublishedEvent): PublishedRecord {
        val record = ProducerRecord(
            event.topic,
            event.key,
            event.payload
        )

        record.headers().add(
            KafkaEventHeaders.EVENT_TYPE,
            event.eventType.toByteArray()
        )

        event.headers.forEach { (key, value) ->
            record.headers().add(key, value.toByteArray())
        }

        return try {
            val metadata = producer.send(record).get()

            PublishedRecord(
                topic = metadata.topic(),
                partition = metadata.partition(),
                offset = metadata.offset(),
                timestamp = metadata.timestamp()
            )
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException(
                "Kafka publication was interrupted",
                exception
            )
        } catch (exception: ExecutionException) {
            throw (exception.cause ?: exception)
        }
    }
}
