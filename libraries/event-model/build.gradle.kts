plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    api(libs.jacksonDatabind)
    api(libs.jacksonKotlin)

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)

    testRuntimeOnly(libs.junitJupiterPlatform)
}