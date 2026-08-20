package com.krizaldis.homepulse.state.domain

class RetryableInfrastructureException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)