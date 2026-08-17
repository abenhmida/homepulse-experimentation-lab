package com.krizaldis.homepulse.kafka

import kotlinx.serialization.json.Json

class JsonEventSerializer(
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
) {
    inline fun <reified T> serialize(value: T): String =
        json.encodeToString(value)
}