package com.krizaldis.homepulse.state.retry

data class RetryContext(private val metadata: RetryMetadata?){
    val attempt: Int
        get() = metadata?.attempt ?: 0
}
