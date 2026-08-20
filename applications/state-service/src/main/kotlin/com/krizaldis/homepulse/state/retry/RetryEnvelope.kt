package com.krizaldis.homepulse.state.retry

data class RetryEnvelope(
    val metadata: RetryMetadata,
    val payload: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RetryEnvelope

        if (metadata != other.metadata) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = metadata.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
