package com.krizaldis.homepulse.state.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ProcessingResultTest {
    @Test
    fun `applied is terminal success`() {

        val result = ProcessingResult.Applied

        assertTrue(
            result is ProcessingResult.Applied
        )
    }

    @Test
    fun `duplicate is terminal success`() {

        val result =
            ProcessingResult.Duplicate

        assertTrue(
            result is ProcessingResult.Duplicate
        )
    }

    @Test
    fun `stale is terminal success`() {

        val result = ProcessingResult.Stale

        assertTrue(
            result is ProcessingResult.Stale
        )
    }
}