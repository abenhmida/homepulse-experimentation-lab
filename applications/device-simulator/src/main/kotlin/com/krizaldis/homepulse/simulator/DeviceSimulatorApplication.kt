package com.krizaldis.homepulse.simulator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DeviceSimulatorApplication

fun main(args: Array<String>) {
    runApplication<DeviceSimulatorApplication>(*args)
}
