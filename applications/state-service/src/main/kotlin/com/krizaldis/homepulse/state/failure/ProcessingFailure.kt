package com.krizaldis.homepulse.state.error

data class ProcessingFailure(
    val category: ErrorCategory,
    val code: String,
    val message: String,
    val cause: Throwable? = null
)