package com.krizaldis.homepulse.state.domain
enum class FailureType {

    /**
     * No failure occurred.
     */
    NONE,

    /**
     * The event was already processed.
     */
    DUPLICATE,

    /**
     * The event is older than the current projection.
     */
    STALE,

    /**
     * Failure may succeed if processing is attempted again.
     */
    RETRYABLE,

    /**
     * Reprocessing the same event will not fix the problem.
     */
    PERMANENT
}