package com.krizaldis.homepulse.state.config

import com.krizaldis.homepulse.state.application.RetryProcessingService
import com.krizaldis.homepulse.state.application.StateService
import com.krizaldis.homepulse.state.retry.RetryEnvelopeFactory
import com.krizaldis.homepulse.state.retry.RetryMessagePublisher
import com.krizaldis.homepulse.state.retry.RetryPolicy
import com.krizaldis.homepulse.state.retry.RetryProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class RetryProcessingConfiguration {

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun retryEnvelopeFactory(): RetryEnvelopeFactory = RetryEnvelopeFactory()

    @Bean
    fun retryProcessor(
        stateService: StateService,
        retryPolicy: RetryPolicy,
        retryEnvelopeFactory: RetryEnvelopeFactory,
        retryMessagePublisher: RetryMessagePublisher,
        clock: Clock
    ): RetryProcessor = RetryProcessingService(
        stateService = stateService,
        retryPolicy = retryPolicy,
        retryEnvelopeFactory = retryEnvelopeFactory,
        publisher = retryMessagePublisher,
        clock = clock
    )
}
