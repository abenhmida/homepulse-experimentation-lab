package com.krizaldis.homepulse.state.failure

sealed interface ProcessingResult {
    data object Applied : ProcessingResult
    data object Duplicate : ProcessingResult
    data object Stale : ProcessingResult

    data class Retry(
        val cause: Throwable
    ) : ProcessingResult

    data class DeadLetter(
        val cause: Throwable
    ) : ProcessingResult
}