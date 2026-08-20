package com.krizaldis.homepulse.serialization

import com.fasterxml.jackson.databind.JavaType
import com.krizaldis.homepulse.event.DomainEvent

class EventSerializer {

    fun <T> serialize(event: DomainEvent<T>): ByteArray =
        JsonMapper.mapper.writeValueAsBytes(event)

    fun <T> deserialize(
        bytes: ByteArray,
        payloadType: Class<T>
    ): DomainEvent<T> {

        require(bytes.isNotEmpty()) {
            "Event payload must not be empty"
        }

        val javaType: JavaType =
            JsonMapper.mapper.typeFactory.constructParametricType(
                DomainEvent::class.java,
                payloadType
            )

        return JsonMapper.mapper.readValue(
            bytes,
            javaType
        )
    }
}