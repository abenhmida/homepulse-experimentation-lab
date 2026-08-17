plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

application {
    mainClass.set("com.krizaldis.homepulse.state.StateServiceMainKt")  // Set your main class
}

dependencies {
    implementation(project(":libraries:event-model"))
    implementation(project(":libraries:kafka-common"))

    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationJson)

    implementation(libs.kafkaClients)
    implementation(libs.jacksonDatabind)
    implementation(libs.jacksonKotlin)
    implementation(libs.slf4jApi)

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
}