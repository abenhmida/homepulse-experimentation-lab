package com.krizaldis.homepulse.state.failure

import com.krizaldis.homepulse.state.failure.FailureType.DUPLICATE
import com.krizaldis.homepulse.state.failure.FailureType.PERMANENT
import com.krizaldis.homepulse.state.failure.FailureType.RETRYABLE
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputExceededException
import software.amazon.awssdk.services.dynamodb.model.ThrottlingException

class FailureClassifier {
    fun classify(exception: Throwable): FailureType = when (exception) {
        is ConditionalCheckFailedException -> DUPLICATE
        is ProvisionedThroughputExceededException -> RETRYABLE
        is ThrottlingException -> RETRYABLE
        is SdkClientException -> RETRYABLE
        is IllegalArgumentException -> PERMANENT
        else -> PERMANENT
    }
}