package com.krizaldis.homepulse.state.domain

sealed interface ProcessingResult {
    /**
     * Event was successfully applied to the projection.
     */
    data object Applied : ProcessingResult
    /**
     * Event was already successfully processed.
     *
     * This is a terminal success from the consumer's
     * perspective and the Kafka offset can be committed.
     */
    data object Duplicate : ProcessingResult
    /**
     * Event is valid but older than the state currently
     * stored for the device.
     */
    data object Stale : ProcessingResult

    /**
     * Processing failed, but the failure is expected to
     * be transient and the event should be retried.
     */
    data class RetryableFailure(
        val cause: Throwable
    ) : ProcessingResult

    /**
     * Processing failed permanently and the event should
     * eventually be sent to the DLQ.
     */
    data class PermanentFailure(
        val cause: Throwable
    ) : ProcessingResult
}