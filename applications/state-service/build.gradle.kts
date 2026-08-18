plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.dependencyManagement)
    id("org.graalvm.buildtools.native")
    kotlin("plugin.spring") version "2.0.21"
    application
}

application {
    mainClass.set("com.krizaldis.homepulse.state.StateServiceApplicationKt")
}

springBoot {
    mainClass.set("com.krizaldis.homepulse.state.StateServiceApplicationKt")
}

// GraalVM Native configuration
graalvmNative {
    binaries {
        named("main") {
            // Build configuration
            imageName.set("state-service")
            mainClass.set("com.krizaldis.homepulse.state.StateServiceApplicationKt")

            // Build-time arguments
            buildArgs.add("--enable-http")
            buildArgs.add("--enable-https")
            buildArgs.add("--enable-all-security-services")

            // Enable reflection for common patterns
            buildArgs.add("-H:+ReportExceptionStackTraces")
            buildArgs.add("-H:+AddAllCharsets")
            buildArgs.add("-H:IncludeResources=.*\\.properties$")

            // For serialization
            buildArgs.add("--initialize-at-build-time=org.apache.kafka.common.utils.LogContext")
            buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")
        }
    }

    // Configure toolchain
    toolchainDetection = true

    // Agent for metadata collection (use during testing)
    metadataRepository {
        enabled.set(true)
    }
}

dependencies {
    implementation(libs.springBootStarter)
    implementation(libs.springBootStarterWeb)
    implementation(libs.springBootStarterActuator)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.micrometer.registry.prometheus)

    implementation(project(":libraries:event-model"))
    implementation(project(":libraries:kafka-common"))
    implementation(project(":libraries:observability"))

    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationJson)

    implementation(libs.kafkaClients)
    implementation(libs.jacksonDatabind)
    implementation(libs.jacksonKotlin)
    implementation(libs.slf4j.api)

    testImplementation(libs.springBootStarterTest)
    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)

    implementation(platform(libs.awssdkBom))
    implementation(libs.awsDynamodb)

    implementation(libs.micrometerCore)
    implementation(libs.micrometer.registry.prometheus)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Native tests
    systemProperty("spring.native.test", "true")
}

// Generate native configuration metadata
tasks.register<JavaExec>("generateNativeMetadata") {
    mainClass.set("com.krizaldis.homepulse.state.StateServiceApplicationKt")
    classpath = sourceSets.main.get().runtimeClasspath
    systemProperties["spring.native.metadata"] = "true"
    systemProperties["spring.native.metadata.cache"] = "true"
}