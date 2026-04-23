package com.app.douyin.pro.feature.edit

import android.net.Uri
import com.app.douyin.pro.feature.edit.domain.model.ClipItem
import com.app.douyin.pro.feature.edit.domain.model.EditProject
import com.app.douyin.pro.feature.edit.domain.model.EditTrack
import com.app.douyin.pro.feature.edit.domain.model.TrackType
import com.app.douyin.pro.feature.edit.ui.vm.EditViewModel
import com.app.douyin.pro.lib.media.MediaAssetManager
import com.app.douyin.pro.feature.edit.domain.usecase.ExportVideoUseCase
import org.mockito.Mockito.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ClipBoundaryTest {

    private lateinit var viewModel: EditViewModel
    private val exportVideoUseCase = mock(ExportVideoUseCase::class.java)
    private val mediaAssetManager = mock(MediaAssetManager::class.java)
    private val context = mock(android.content.Context::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = EditViewModel(exportVideoUseCase, context, mediaAssetManager)
    }

    @Test
    fun `test update clip boundaries within source duration`() {
        val uri = mock(Uri::class.java)
        viewModel.initProject(uri, 10000L)
        val clipId = viewModel.uiState.value.project.getMainVideoTrack()!!.clips[0].id

        // Normal clipping
        viewModel.updateClipBoundaries(clipId, 1000L, 5000L)
        val clip = viewModel.uiState.value.project.getMainVideoTrack()!!.clips[0]
        assertEquals(1000L, clip.startInSourceMs)
        assertEquals(5000L, clip.endInSourceMs)
    }

    @Test
    fun `test update clip boundaries prevents empty clip`() {
        val uri = mock(Uri::class.java)
        viewModel.initProject(uri, 10000L)
        val clipId = viewModel.uiState.value.project.getMainVideoTrack()!!.clips[0].id

        // Attempt to make start > end
        viewModel.updateClipBoundaries(clipId, 6000L, 5000L)
        val clip = viewModel.uiState.value.project.getMainVideoTrack()!!.clips[0]

        // Validation should ensure end is at least start + 100ms
        assertEquals(6000L, clip.startInSourceMs)
        assertEquals(6100L, clip.endInSourceMs)
    }

    @Test
    fun `test update clip boundaries respects source duration`() {
        val uri = mock(Uri::class.java)
        viewModel.initProject(uri, 5000L)
        val clipId = viewModel.uiState.value.project.getMainVideoTrack()!!.clips[0].id

        // Attempt to exceed source duration
        viewModel.updateClipBoundaries(clipId, 0L, 6000L)
        val clip = viewModel.uiState.value.project.getMainVideoTrack()!!.clips[0]
        assertEquals(0L, clip.startInSourceMs)
        assertEquals(5000L, clip.endInSourceMs)
    }

    @Test
    fun `test cover timestamp selection`() {
        val uri = mock(Uri::class.java)
        viewModel.initProject(uri, 10000L)

        viewModel.setCoverTimestamp(3500L)
        assertEquals(3500L, viewModel.uiState.value.project.coverTimestampMs)
    }
}
