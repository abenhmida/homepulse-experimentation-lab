package com.krizaldis.homepulse.kafka

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.serialization.EventSerializer

class JsonEventDeserializer {
    private val serializer = EventSerializer()

    fun <T> deserialize(value: ByteArray, payloadType: Class<T>): DomainEvent<T> =
        serializer.deserialize(value, payloadType)
}
