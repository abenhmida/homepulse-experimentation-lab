package com.krizaldis.homepulse.kafka

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.serialization.EventSerializer

class JsonEventSerializer {
    private val serializer = EventSerializer()

    fun <T> serialize(event: DomainEvent<T>): ByteArray =
        serializer.serialize(event)
}
