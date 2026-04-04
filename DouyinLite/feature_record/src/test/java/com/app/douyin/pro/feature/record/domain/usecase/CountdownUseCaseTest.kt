package com.app.douyin.pro.feature.record.domain.usecase

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CountdownUseCaseTest {

    private val countdownUseCase = CountdownUseCase()

    @Test
    fun `countdown from 3 should emit 3, 2, 1, 0`() = runBlocking {
        val result = countdownUseCase(3).toList()
        assertEquals(listOf(3, 2, 1, 0), result)
    }

    @Test
    fun `countdown from 0 should emit 0`() = runBlocking {
        val result = countdownUseCase(0).toList()
        assertEquals(listOf(0), result)
    }
}
