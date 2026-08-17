package com.krizaldis.homepulse.kafka

object Environment {
    fun get(name: String, default: String? = null): String {
        return System.getenv(name)
            ?: default
            ?: error("Environment variable $name is not defined")
    }
}