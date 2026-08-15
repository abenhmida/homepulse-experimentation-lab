plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":libraries:event-model"))

    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.slf4jApi)

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
}