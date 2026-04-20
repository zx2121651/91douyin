package com.app.douyin.pro.feature.record.ui.vm

import com.app.douyin.pro.feature.record.domain.usecase.CountdownUseCase
import com.app.douyin.pro.feature.record.domain.usecase.GetAvailableFiltersUseCase
import com.app.douyin.pro.feature.record.ui.state.RecordState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@ExperimentalCoroutinesApi
class RecordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: RecordViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Use real CountdownUseCase as it has no dependencies
        val countdownUseCase = CountdownUseCase()

        // Manual stub for GetAvailableFiltersUseCase
        val getAvailableFiltersUseCase = ManualGetAvailableFiltersUseCase()

        viewModel = RecordViewModel(getAvailableFiltersUseCase, countdownUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testStartRecording() {
        viewModel.startRecording()
        assertEquals(RecordState.RECORDING, viewModel.uiState.value.currentState)
        assertTrue(viewModel.uiState.value.isRecording)
    }

    @Test
    fun testPauseRecording() {
        viewModel.startRecording()
        viewModel.pauseRecording("path/to/video.mp4", 5000L)

        assertEquals(RecordState.PAUSED, viewModel.uiState.value.currentState)
        assertEquals(1, viewModel.uiState.value.segments.size)
        assertEquals(5000L, viewModel.uiState.value.totalDurationMs)
        assertEquals("path/to/video.mp4", viewModel.uiState.value.segments[0].filePath)
    }

    @Test
    fun testDeleteLastSegment() {
        viewModel.startRecording()
        viewModel.pauseRecording("path1.mp4", 3000L)
        viewModel.startRecording()
        viewModel.pauseRecording("path2.mp4", 4000L)

        assertEquals(2, viewModel.uiState.value.segments.size)
        assertEquals(7000L, viewModel.uiState.value.totalDurationMs)

        viewModel.deleteLastSegment()

        assertEquals(1, viewModel.uiState.value.segments.size)
        assertEquals(3000L, viewModel.uiState.value.totalDurationMs)
        assertEquals(RecordState.PAUSED, viewModel.uiState.value.currentState)

        viewModel.deleteLastSegment()
        assertEquals(0, viewModel.uiState.value.segments.size)
        assertEquals(0L, viewModel.uiState.value.totalDurationMs)
        assertEquals(RecordState.IDLE, viewModel.uiState.value.currentState)
    }

    // Manual mock that doesn't use Mockito for the class itself
    private class ManualGetAvailableFiltersUseCase : GetAvailableFiltersUseCase(
        com.app.douyin.pro.feature.record.data.RecordRepository(
            object : com.app.douyin.pro.feature.record.data.source.RecordDataSource {
                override suspend fun getFilters() = emptyList<com.app.douyin.pro.feature.record.domain.model.FilterEffect>()
                override suspend fun publishVideo(videoFile: java.io.File, title: String) {}
            }
        )
    )
}
