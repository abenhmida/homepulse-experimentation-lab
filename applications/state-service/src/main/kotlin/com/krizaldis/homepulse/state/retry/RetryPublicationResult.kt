package com.krizaldis.homepulse.state.retry

data class RetryPublicationResult(
    val topic: String,
    val partition: Int,
    val offset: Long
)
