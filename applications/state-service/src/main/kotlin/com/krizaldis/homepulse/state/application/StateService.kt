package com.krizaldis.homepulse.state.application

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.state.domain.FailureClassifier
import com.krizaldis.homepulse.state.domain.FailureType
import com.krizaldis.homepulse.state.domain.ProcessingResult
import com.krizaldis.homepulse.state.domain.ProjectionResult
import com.krizaldis.homepulse.state.domain.StateRepository
import com.krizaldis.homepulse.state.projection.ProjectionDispatcher
import org.springframework.stereotype.Component

@Component
class StateService(
    private val repository: StateRepository,
    private val projectionDispatcher: ProjectionDispatcher,
    private val failureClassifier: FailureClassifier
) {

    fun process(event: DomainEvent<*>): ProcessingResult {

        return try {

            val projection =
                projectionDispatcher.dispatch(event)

            when (
                repository.apply(
                    event = event,
                    projection = projection
                )
            ) {
                ProjectionResult.Applied ->
                    ProcessingResult.Applied

                ProjectionResult.Duplicate ->
                    ProcessingResult.Duplicate

                ProjectionResult.Stale ->
                    ProcessingResult.Stale
            }

        } catch (exception: Exception) {

            val failureType = failureClassifier.classify(exception)

            when (failureType) {
                FailureType.RETRYABLE ->
                    ProcessingResult.RetryableFailure(exception)

                FailureType.PERMANENT ->
                    ProcessingResult.PermanentFailure(exception)

                else ->
                    throw IllegalStateException(
                        "Unexpected failure classification: $failureType",
                        exception
                    )
            }
        }
    }
}
