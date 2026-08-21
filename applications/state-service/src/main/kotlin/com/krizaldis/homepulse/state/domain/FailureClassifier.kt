package com.krizaldis.homepulse.state.domain

import com.krizaldis.homepulse.state.failure.FailureType
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputExceededException
import software.amazon.awssdk.services.dynamodb.model.ThrottlingException

class FailureClassifier {
    fun classify(throwable: Throwable): FailureType {
        return when (throwable) {
            is InvalidEventException ->
                FailureType.PERMANENT

            is UnsupportedEventException ->
                FailureType.PERMANENT

            is IllegalArgumentException ->
                FailureType.PERMANENT

            is ProvisionedThroughputExceededException ->
                FailureType.RETRYABLE

            is ThrottlingException ->
                FailureType.RETRYABLE

            is SdkClientException ->
                FailureType.RETRYABLE

            is ProjectionException ->
                classifyProjectionException(
                    throwable
                )
            is RetryableInfrastructureException ->
                FailureType.RETRYABLE

            else ->
                classifyUnknown(throwable)
        }
    }

    private fun classifyProjectionException(
        exception: ProjectionException
    ): FailureType {

        val cause = exception.cause

        return if (cause != null) {
            classify(cause)
        } else {
            FailureType.RETRYABLE
        }
    }

    private fun classifyUnknown(
        throwable: Throwable
    ): FailureType {

        return when (throwable) {

            is NullPointerException ->
                FailureType.PERMANENT

            is IllegalStateException ->
                FailureType.PERMANENT

            else ->
                FailureType.RETRYABLE
        }
    }
}