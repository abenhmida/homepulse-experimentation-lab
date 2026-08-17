plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.krizaldis.homepulse.simulator.SimulatorMainKt")
}

dependencies {
    implementation(project(":libraries:event-model"))
    implementation(project(":libraries:kafka-common"))

    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationJson)

    implementation(libs.slf4jApi)

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
}