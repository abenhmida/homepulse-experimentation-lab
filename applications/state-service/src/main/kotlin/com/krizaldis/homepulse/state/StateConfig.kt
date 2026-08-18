package com.krizaldis.homepulse.state

data class StateConfig(
    val bootstrapServers: String = env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
    val inputTopic: String = env("KAFKA_TOPIC", "home.events.v1"),
    val retryTopic: String = env("KAFKA_RETRY_TOPIC", "home.events.retry.1m"),
    val dlqTopic: String = env("KAFKA_DLQ_TOPIC", "home.events.dlq"),
    val groupId: String = env("KAFKA_GROUP_ID", "homepulse-state-service"),
    val dynamoEndpoint: String? = System.getenv("DYNAMODB_ENDPOINT"),
    val awsRegion: String = env("AWS_REGION", "eu-west-3"),
    val stateTable: String = env("DYNAMODB_STATE_TABLE", "homepulse-device-state"),
    val idempotencyTable: String = env("DYNAMODB_IDEMPOTENCY_TABLE", "homepulse-idempotency"),
    val retryAttempts: Int = env("MAX_RETRY_ATTEMPTS", "3").toInt()
)

private fun env(name: String, default: String): String =
    System.getenv(name) ?: default