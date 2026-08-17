plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.dependencyManagement)
    kotlin("plugin.spring") version "2.0.21"
    application
}

application {
    mainClass.set("com.krizaldis.homepulse.simulator.DeviceSimulatorApplicationKt")  // Set your main class
}

dependencies {
    implementation(libs.springBootStarter)
    implementation(libs.springBootStarterActuator)
    implementation(libs.micrometer.registry.prometheus)

    implementation(project(":libraries:event-model"))
    implementation(project(":libraries:kafka-common"))

    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationJson)

    implementation(libs.slf4jApi)

    testImplementation(libs.springBootStarterTest)
    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}