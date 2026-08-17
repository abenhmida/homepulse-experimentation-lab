plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.jacksonDatabind)
    api(libs.jacksonKotlin)
    api(libs.jacksonDatatypeJsr310)

    implementation(project(":libraries:event-model"))

    implementation(libs.kotlinxSerializationJson)
    implementation(libs.kotlinxCoroutinesCore)

    api(libs.kafkaClients)

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)

    testRuntimeOnly(libs.junitJupiterPlatform)
}