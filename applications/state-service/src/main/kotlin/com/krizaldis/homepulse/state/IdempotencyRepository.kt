package com.krizaldis.homepulse.state

interface IdempotencyRepository {
    suspend fun tryAcquire(
        eventId: String,
        deviceId: String
    ): Boolean
}