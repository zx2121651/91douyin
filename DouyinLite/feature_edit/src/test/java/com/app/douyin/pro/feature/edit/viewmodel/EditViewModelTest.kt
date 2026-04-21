package com.app.douyin.pro.feature.edit.viewmodel

import android.net.Uri
import com.app.douyin.pro.feature.edit.domain.model.TrackType
import com.app.douyin.pro.feature.edit.domain.usecase.ExportVideoUseCase
import com.app.douyin.pro.feature.edit.ui.vm.EditViewModel
import com.app.douyin.pro.lib.media.MediaAssetManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import android.content.Context

class EditViewModelTest {

    @Mock
    lateinit var exportVideoUseCase: ExportVideoUseCase

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var mockUri: Uri

    @Mock
    lateinit var mediaAssetManager: MediaAssetManager

    private lateinit var viewModel: EditViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = EditViewModel(exportVideoUseCase, context, mediaAssetManager)
    }

    @Test
    fun `initProject should create a video track with one clip`() {
        viewModel.initProject(mockUri, 10000L)

        val state = viewModel.uiState.value
        assertEquals(1, state.tracks.size)
        assertEquals(TrackType.VIDEO, state.tracks[0].type)
        assertEquals(1, state.tracks[0].clips.size)
        assertEquals(10000L, state.totalDurationMs)
    }

    @Test
    fun `splitClip should divide one clip into two`() {
        viewModel.initProject(mockUri, 10000L)
        viewModel.updateCurrentTime(5000L)
        viewModel.splitClip()

        val state = viewModel.uiState.value
        val videoTrack = state.tracks.find { it.type == TrackType.VIDEO }
        assertEquals(2, videoTrack?.clips?.size)
        assertEquals(0L, videoTrack?.clips?.get(0)?.startInSourceMs)
        assertEquals(5000L, videoTrack?.clips?.get(0)?.endInSourceMs)
        assertEquals(5000L, videoTrack?.clips?.get(1)?.startInSourceMs)
        assertEquals(10000L, videoTrack?.clips?.get(1)?.endInSourceMs)
    }

    @Test
    fun `deleteSelectedClip should remove the clip from track`() {
        viewModel.initProject(mockUri, 10000L)
        val clipId = viewModel.uiState.value.tracks[0].clips[0].id
        viewModel.selectClip(clipId)
        viewModel.deleteSelectedClip()

        val state = viewModel.uiState.value
        assertTrue(state.tracks[0].clips.isEmpty())
    }

    @Test
    fun `undo should revert the last operation`() {
        viewModel.initProject(mockUri, 10000L)
        val originalClipCount = viewModel.uiState.value.tracks[0].clips.size

        viewModel.updateCurrentTime(5000L)
        viewModel.splitClip()
        assertEquals(2, viewModel.uiState.value.tracks[0].clips.size)

        viewModel.undo()
        assertEquals(originalClipCount, viewModel.uiState.value.tracks[0].clips.size)
    }
}
