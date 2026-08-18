plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {

    implementation(libs.kafkaClients)
    api(libs.otel.api)
    implementation(libs.otel.sdk)
    implementation(libs.otel.sdk.trace)
    implementation(libs.otel.sdk.metrics)
    implementation(libs.otel.exporter.otlp)
    implementation(libs.otel.semconv)

    implementation(libs.slf4j.api)
    testImplementation(libs.kotlin.test)
}