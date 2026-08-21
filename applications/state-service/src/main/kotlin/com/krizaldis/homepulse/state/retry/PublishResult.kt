package com.krizaldis.homepulse.state.retry

sealed interface PublishResult {

    data object Published : PublishResult
    data class Failed(val cause: Throwable) : PublishResult
}