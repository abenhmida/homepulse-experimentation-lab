package com.krizaldis.homepulse.state

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import java.time.Duration

@SpringBootApplication
class StateServiceApplication

fun main(args: Array<String>) {
    runApplication<StateServiceApplication>(*args)
}
