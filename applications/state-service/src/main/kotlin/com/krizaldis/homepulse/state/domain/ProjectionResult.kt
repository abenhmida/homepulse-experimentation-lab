package com.krizaldis.homepulse.state.domain

sealed interface ProjectionResult {
    data object Applied : ProjectionResult
    data object Duplicate : ProjectionResult
    data object Stale : ProjectionResult
}