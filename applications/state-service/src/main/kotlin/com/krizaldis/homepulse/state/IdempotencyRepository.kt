package com.krizaldis.homepulse.state

interface IdempotencyRepository {
    suspend fun tryAquire(
        eventId: String
    ): Boolean
}