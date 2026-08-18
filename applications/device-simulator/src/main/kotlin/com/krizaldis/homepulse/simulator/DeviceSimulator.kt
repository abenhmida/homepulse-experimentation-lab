package com.krizaldis.homepulse.simulator

import com.krizaldis.homepulse.observability.Telemetry
import com.krizaldis.homepulse.observability.TelemetryConfig
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DeviceSimulator(
    private val json: Json = Json { encodeDefaults = true }
) {
    private val logger = LoggerFactory.getLogger(DeviceSimulator::class.java)
    private var config = ProducerConfig()

    val telemetry = Telemetry.start(
        TelemetryConfig(
            serviceName = "homepulse-event-producer"
        )
    )

    private val producer = EventProducer(config, telemetry)

    @PostConstruct
    fun start() {
        logger.info("Starting HomePulse event producer: {}", config)
        producer.run()
    }

    @PreDestroy
    fun stop() {
        logger.info("Stopping Device Simulator...")
        producer.close()
    }
}
