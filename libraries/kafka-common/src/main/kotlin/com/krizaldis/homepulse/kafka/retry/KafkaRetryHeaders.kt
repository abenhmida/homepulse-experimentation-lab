package com.krizaldis.homepulse.kafka.retry

object KafkaRetryHeaders {
    const val ATTEMPT = "homepulse-retry-attempt"
    const val ORIGINAL_EVENT_ID = "homepulse-original-event-id"
    const val ORIGINAL_TOPIC = "homepulse-original-topic"
    const val ORIGINAL_PARTITION = "homepulse-original-partition"
    const val ORIGINAL_OFFSET = "homepulse-original-offset"
    const val FAILURE_TYPE = "homepulse-failure-type"
    const val EXCEPTION_TYPE = "homepulse-exception-type"
    const val FIRST_FAILED_AT = "homepulse-first-failed-at"
    const val LAST_FAILED_AT = "homepulse-last-failed-at"
    const val NEXT_ATTEMPT_AT = "homepulse-next-attempt-at"
    const val CORRELATION_ID = "correlation-id"
    const val CAUSATION_ID = "causation-id"
}
