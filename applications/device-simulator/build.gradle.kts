plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.dependencyManagement)
    id("org.graalvm.buildtools.native")
    kotlin("plugin.spring") version "2.0.21"
    application
}

application {
    mainClass.set("com.krizaldis.homepulse.simulator.DeviceSimulatorApplicationKt")
}

dependencies {
    implementation(libs.springBootStarter)
    implementation(libs.springBootStarterActuator)
    implementation(libs.micrometer.registry.prometheus)

    implementation(project(":libraries:event-model"))
    implementation(project(":libraries:kafka-common"))
    implementation(project(":libraries:observability"))

    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationJson)

    implementation(libs.slf4j.api)

    testImplementation(libs.springBootStarterTest)
    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}