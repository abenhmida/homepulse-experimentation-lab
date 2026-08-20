package com.krizaldis.homepulse.state.application

import com.krizaldis.homepulse.event.DomainEvent
import com.krizaldis.homepulse.event.EventMetadata
import com.krizaldis.homepulse.event.TemperatureReported
import com.krizaldis.homepulse.state.domain.FailureClassifier
import com.krizaldis.homepulse.state.domain.FailureType
import com.krizaldis.homepulse.state.domain.InvalidEventException
import com.krizaldis.homepulse.state.domain.ProcessingResult
import com.krizaldis.homepulse.state.domain.ProjectionCommand
import com.krizaldis.homepulse.state.domain.ProjectionResult
import com.krizaldis.homepulse.state.domain.ProjectionValue
import com.krizaldis.homepulse.state.domain.RetryableInfrastructureException
import com.krizaldis.homepulse.state.domain.StateRepository
import com.krizaldis.homepulse.state.projection.ProjectionDispatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class StateServiceTest {

    private lateinit var repository: StateRepository
    private lateinit var projectionDispatcher: ProjectionDispatcher
    private lateinit var failureClassifier: FailureClassifier
    private lateinit var sut: StateService

    private lateinit var event: DomainEvent<TemperatureReported>
    private lateinit var projection: ProjectionCommand

    @BeforeEach
    fun setUp() {

        repository = mockk()
        projectionDispatcher = mockk()
        failureClassifier = mockk()

        sut =
            StateService(
                repository = repository,
                projectionDispatcher = projectionDispatcher,
                failureClassifier = failureClassifier
            )

        event =
            DomainEvent(
                metadata =
                    EventMetadata(
                        eventId = "evt-001",
                        eventType = "home.device.temperature-reported",
                        schemaVersion = 1,
                        homeId = "home-001",
                        deviceId = "thermostat-01",
                        occurredAt =
                            Instant.parse(
                                "2026-08-20T15:00:00Z"
                            ),
                        sequenceNumber = 42,
                        correlationId = "corr-001",
                        causationId = null
                    ),
                payload =
                    TemperatureReported(
                        temperatureCelsius = 21.5,
                        humidityPercent = 47.0
                    )
            )

        projection =
            ProjectionCommand(
                projectionType = "device-state",
                partitionKey = "HOME#home-001",
                sortKey = "DEVICE#thermostat-01",
                sequenceNumber = 42,
                attributes =
                    mapOf(
                        "temperatureCelsius" to
                            ProjectionValue.NumberValue(21.5)
                    )
            )
    }

    @Test
    fun `should return Applied when repository applies projection`() {

        every {
            projectionDispatcher.dispatch(event)
        } returns projection

        every {
            repository.apply(
                event = event,
                projection = projection
            )
        } returns ProjectionResult.Applied

        val result = sut.process(event)

        assertEquals(
            ProcessingResult.Applied,
            result
        )

        verify(exactly = 1) {
            projectionDispatcher.dispatch(event)
        }

        verify(exactly = 1) {
            repository.apply(
                event = event,
                projection = projection
            )
        }
    }

    @Test
    fun `should return Duplicate when repository detects duplicate`() {

        every {
            projectionDispatcher.dispatch(event)
        } returns projection

        every {
            repository.apply(
                event = event,
                projection = projection
            )
        } returns ProjectionResult.Duplicate

        val result = sut.process(event)

        assertEquals(
            ProcessingResult.Duplicate,
            result
        )

        verify(exactly = 1) {
            repository.apply(
                event = event,
                projection = projection
            )
        }
    }

    @Test
    fun `should return Stale when repository detects stale event`() {

        every {
            projectionDispatcher.dispatch(event)
        } returns projection

        every {
            repository.apply(
                event = event,
                projection = projection
            )
        } returns ProjectionResult.Stale

        val result = sut.process(event)

        assertEquals(
            ProcessingResult.Stale,
            result
        )

        verify(exactly = 1) {
            repository.apply(
                event = event,
                projection = projection
            )
        }
    }

    @Test
    fun `should return RetryableFailure for retryable exception from repository`() {

        val exception =
            RetryableInfrastructureException(
                message = "DynamoDB temporarily unavailable"
            )

        every {
            projectionDispatcher.dispatch(event)
        } returns projection

        every {
            repository.apply(
                event = event,
                projection = projection
            )
        } throws exception

        every {
            failureClassifier.classify(exception)
        } returns FailureType.RETRYABLE

        val result = sut.process(event)

        assertInstanceOf(
            ProcessingResult.RetryableFailure::class.java,
            result
        )

        assertEquals(
            exception,
            (result as ProcessingResult.RetryableFailure).cause
        )

        verify(exactly = 1) {
            failureClassifier.classify(exception)
        }
    }

    @Test
    fun `should return PermanentFailure for invalid event`() {

        val exception =
            InvalidEventException(
                message = "temperature is invalid"
            )

        every {
            projectionDispatcher.dispatch(event)
        } returns projection

        every {
            repository.apply(
                event = event,
                projection = projection
            )
        } throws exception

        every {
            failureClassifier.classify(exception)
        } returns FailureType.PERMANENT

        val result = sut.process(event)

        assertInstanceOf(
            ProcessingResult.PermanentFailure::class.java,
            result
        )

        assertEquals(
            exception,
            (result as ProcessingResult.PermanentFailure).cause
        )
    }

    @Test
    fun `should preserve original exception`() {

        val exception =
            RetryableInfrastructureException(
                message = "temporary database failure"
            )

        every {
            projectionDispatcher.dispatch(event)
        } throws exception

        every {
            failureClassifier.classify(exception)
        } returns FailureType.RETRYABLE

        val result = sut.process(event)

        assertInstanceOf(
            ProcessingResult.RetryableFailure::class.java,
            result
        )

        assertEquals(
            exception,
            (result as ProcessingResult.RetryableFailure).cause
        )

        verify(exactly = 0) {
            repository.apply(
                event = any(),
                projection = any()
            )
        }
    }

    @Test
    fun `permanent projection failure should not call repository`() {

        val exception =
            InvalidEventException(
                message = "unsupported projection payload"
            )

        every {
            projectionDispatcher.dispatch(event)
        } throws exception

        every {
            failureClassifier.classify(exception)
        } returns FailureType.PERMANENT

        val result = sut.process(event)

        assertInstanceOf(
            ProcessingResult.PermanentFailure::class.java,
            result
        )

        verify(exactly = 0) {
            repository.apply(
                event = any(),
                projection = any()
            )
        }
    }
}
