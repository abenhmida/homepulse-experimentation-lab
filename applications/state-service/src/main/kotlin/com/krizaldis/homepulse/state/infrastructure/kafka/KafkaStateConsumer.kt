package com.krizaldis.homepulse.state.infrastructure.kafka

import com.krizaldis.homepulse.state.application.StateService
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KafkaStateConsumer(
    private val stateService: StateService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var consumerJob: Job? = null

    @PostConstruct
    fun start() {
        consumerJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                stateService.run()
            } catch (e: Exception) {
                logger.error("State consumer stopped unexpectedly", e)
            }
        }
    }

    @PreDestroy
    fun stop() {
        stateService.close()
        consumerJob?.cancel()
    }
}
