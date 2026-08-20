package com.krizaldis.homepulse.state.domain

class UnsupportedEventException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)