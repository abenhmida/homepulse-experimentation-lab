package com.krizaldis.homepulse.state.domain

import com.krizaldis.homepulse.state.failure.FailureType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.dynamodb.model.ThrottlingException

class FailureClassifierTest {
    private val sut = FailureClassifier()

    @Test
    fun `invalid event is permanent`() {

        val result =
            sut.classify(
                InvalidEventException(
                    "temperature is invalid"
                )
            )

        assertEquals(
            FailureType.PERMANENT,
            result
        )
    }

    @Test
    fun `unsupported event is permanent`() {

        val result =
            sut.classify(
                UnsupportedEventException(
                    "event version 99"
                )
            )

        assertEquals(
            FailureType.PERMANENT,
            result
        )
    }

    @Test
    fun `dynamodb throttling is retryable`() {

        val result =
            sut.classify(
                ThrottlingException.builder()
                    .message("throttled")
                    .build()
            )

        assertEquals(
            FailureType.RETRYABLE,
            result
        )
    }

    @Test
    fun `illegal argument is permanent`() {

        val result =
            sut.classify(
                IllegalArgumentException(
                    "invalid value"
                )
            )

        assertEquals(
            FailureType.PERMANENT,
            result
        )
    }
}