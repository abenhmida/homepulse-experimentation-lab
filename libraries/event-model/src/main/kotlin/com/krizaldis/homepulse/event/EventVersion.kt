package com.krizaldis.homepulse.event

@JvmInline
value class EventVersion(
    val value: Int
) {
    init {
        require(value > 0) {
            "Event version must be greater than zero"
        }
    }
}