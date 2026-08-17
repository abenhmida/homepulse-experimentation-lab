plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.dependencyManagement)
    kotlin("plugin.spring") version "2.0.21"
    application
}

application {
    mainClass.set("com.krizaldis.homepulse.state.StateServiceMainKt")  // Set your main class
}


dependencies {
    implementation(libs.springBootStarter)
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterActuator)
    implementation(libs.micrometer.registry.prometheus)

    implementation(project(":libraries:event-model"))
    implementation(project(":libraries:kafka-common"))

    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationJson)

    implementation(libs.kafkaClients)
    implementation(libs.jacksonDatabind)
    implementation(libs.jacksonKotlin)
    implementation(libs.slf4jApi)

    testImplementation(libs.springBootStarterTest)
    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)

    implementation(platform(libs.awssdkBom))
    implementation(libs.awsDynamodb)

    implementation(libs.micrometerCore)
    implementation(libs.micrometer.registry.prometheus)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
