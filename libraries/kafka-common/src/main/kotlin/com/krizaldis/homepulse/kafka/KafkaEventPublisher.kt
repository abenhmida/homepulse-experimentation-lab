package com.krizaldis.homepulse.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class KafkaEventPublisher(
    private val producer: KafkaProducer<String, String>
) : EventPublisher {
    override suspend fun publish(event: PublishedEvent): PublishedRecord {
        val record = ProducerRecord(
            event.topic,
            event.key,
            event.payload,
        )

        record.headers().add(
            "event-type", event.eventType.toByteArray()
        )

        event.headers.forEach { (key, value) ->
            record.headers().add(
                key, value.toByteArray()
            )
        }

        return suspendCancellableCoroutine { continuation ->
            val future = producer.send(record) { metadata, exception ->
                if (exception != null) {
                    continuation.resumeWithException(exception)
                } else {
                    continuation.resume(
                        PublishedRecord(
                            topic = metadata.topic(),
                            partition = metadata.partition(),
                            offset = metadata.offset(),
                            timestamp = metadata.timestamp()
                        )
                    )
                }
            }
            continuation.invokeOnCancellation {
                future.cancel(false)
            }
        }
    }
}