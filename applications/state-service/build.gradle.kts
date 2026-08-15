plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":libraries:event-model"))

    implementation(libs.kafkaClients)
    implementation(libs.jacksonDatabind)
    implementation(libs.jacksonKotlin)
    implementation(libs.slf4jApi)

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
}