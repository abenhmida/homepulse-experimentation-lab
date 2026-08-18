package com.krizaldis.homepulse.kafka

import kotlinx.serialization.json.Json

class JsonEventDeserializer(
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
) {
    inline fun <reified T> deserialize(value: ByteArray?): T =
        json.decodeFromString(value.toString())
}