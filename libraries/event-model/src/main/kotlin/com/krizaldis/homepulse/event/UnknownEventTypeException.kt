package com.krizaldis.homepulse.event

class UnknownEventTypeException(
    eventType: String
) : RuntimeException(
    "Unknown event type: $eventType"
)