package com.krizaldis.homepulse.state.infrastructure.kafka

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.DoorStateChanged
import com.krizaldis.homepulse.event.EnergyMeasured
import com.krizaldis.homepulse.event.EventType
import com.krizaldis.homepulse.event.LightStateChanged
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.event.UnknownEventTypeException
import com.krizaldis.homepulse.event.retry.RetryEnvelope
import com.krizaldis.homepulse.serialization.JsonMapper
import com.krizaldis.homepulse.telemetry.MotionDetected
import org.springframework.stereotype.Component

@Component
class EventJsonCodec {

    fun deserializeEvent(value: String): DomainEvent<*> {
        val root = JsonMapper.mapper.readTree(value)
        return deserializeEvent(root, root["metadata"]["eventType"].asText())
    }

    fun deserializeRetryEnvelope(value: String): RetryEnvelope {
        val root = JsonMapper.mapper.readTree(value)
        val retry = JsonMapper.mapper.treeToValue(
            root["retry"],
            com.krizaldis.homepulse.event.retry.RetryMetadata::class.java
        )
        return RetryEnvelope(
            retry = retry,
            event = deserializeEvent(root["event"], root["event"]["metadata"]["eventType"].asText())
        )
    }

    private fun deserializeEvent(
        node: JsonNode,
        eventType: String
    ): DomainEvent<*> = when (eventType) {
        EventType.TEMPERATURE_REPORTED.wireName -> convert<TemperatureReported>(node)
        EventType.DOOR_STATE_CHANGED.wireName -> convert<DoorStateChanged>(node)
        EventType.LIGHT_STATE_CHANGED.wireName -> convert<LightStateChanged>(node)
        EventType.ENERGY_MEASURED.wireName -> convert<EnergyMeasured>(node)
        EventType.MOTION_DETECTED.wireName -> convert<MotionDetected>(node)
        else -> throw UnknownEventTypeException(eventType)
    }

    private inline fun <reified T> convert(node: JsonNode): DomainEvent<T> =
        JsonMapper.mapper.convertValue(node, domainEventType<T>())

    private inline fun <reified T> domainEventType(): JavaType =
        JsonMapper.mapper.typeFactory.constructParametricType(
            DomainEvent::class.java,
            T::class.java
        )
}
