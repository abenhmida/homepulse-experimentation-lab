package com.krizaldis.homepulse.state.retry

object RetryHeaders {

    const val EVENT_ID = "x-event-id"

    const val ORIGINAL_TOPIC =
        "x-original-topic"

    const val ORIGINAL_PARTITION =
        "x-original-partition"

    const val ORIGINAL_OFFSET =
        "x-original-offset"

    const val ATTEMPT =
        "x-retry-attempt"

    const val FIRST_FAILURE_AT =
        "x-first-failure-at"

    const val LAST_FAILURE_AT =
        "x-last-failure-at"

    const val NEXT_ATTEMPT_AT =
        "x-next-attempt-at"

    const val FAILURE_TYPE =
        "x-failure-type"

    const val FAILURE_CLASS =
        "x-failure-class"

    const val FAILURE_MESSAGE =
        "x-failure-message"
}