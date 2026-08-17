package com.krizaldis.homepulse.state

import com.krizaldis.homepulse.kafka.Environment
import com.krizaldis.homepulse.kafka.KafkaConsumerFactory
import com.krizaldis.homepulse.kafka.KafkaEventConsumer
import com.krizaldis.homepulse.kafka.KafkaProperties
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val consumer =
        KafkaConsumerFactory(
            KafkaProperties(
                bootstrapServers = Environment.get(
                    "KAFKA_BOOTSTRAP_SERVERS",
                    "localhost:9092"
                ),
                clientId = "state-service2",
                groupId = "state-service"
            )
        ).create()

    val eventConsumer =
        KafkaEventConsumer(
            consumer = consumer,
            topics = listOf("home.device.events")
        ) { record ->

            println(
                "Received " +
                        "topic=${record.topic()} " +
                        "partition=${record.partition()} " +
                        "offset=${record.offset()} " +
                        "key=${record.key()} " +
                        "value=${record.value()}"
            )
        }

    eventConsumer.start()
}