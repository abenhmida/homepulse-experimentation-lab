package com.krizaldis.homepulse.state.domain

class ProjectionException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)