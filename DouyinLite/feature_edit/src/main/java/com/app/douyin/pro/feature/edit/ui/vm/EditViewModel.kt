package com.app.douyin.pro.feature.edit.ui.vm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.edit.domain.model.ClipItem
import com.app.douyin.pro.feature.edit.domain.model.EditProject
import com.app.douyin.pro.feature.edit.domain.model.EditTrack
import com.app.douyin.pro.feature.edit.domain.model.TrackType
import com.app.douyin.pro.feature.edit.ui.state.EditUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    fun initProject(videoUri: Uri, duration: Long) {
        val initialClip = ClipItem(
            sourceUri = videoUri,
            sourceDurationMs = duration,
            endInSourceMs = duration
        )
        val videoTrack = EditTrack(type = TrackType.VIDEO, clips = mutableListOf(initialClip))

        _uiState.update {
            it.copy(
                tracks = listOf(videoTrack),
                totalDurationMs = duration
            )
        }
    }

    fun splitClip() {
        val currentTime = _uiState.value.currentTimeMs
        val tracks = _uiState.value.tracks.toMutableList()

        // Logic for splitting the first video track's clip at currentTime
        val videoTrack = tracks.find { it.type == TrackType.VIDEO } ?: return
        val clips = videoTrack.clips

        var accumulatedTime = 0L
        var splitTargetIndex = -1

        for (i in clips.indices) {
            val clipDuration = clips[i].getTimelineDurationMs()
            if (currentTime > accumulatedTime && currentTime < accumulatedTime + clipDuration) {
                splitTargetIndex = i
                break
            }
            accumulatedTime += clipDuration
        }

        if (splitTargetIndex != -1) {
            val targetClip = clips[splitTargetIndex]
            val splitOffsetInClip = (currentTime - accumulatedTime) * targetClip.speed

            val newClip1 = targetClip.copy(endInSourceMs = targetClip.startInSourceMs + splitOffsetInClip.toLong())
            val newClip2 = targetClip.copy(startInSourceMs = targetClip.startInSourceMs + splitOffsetInClip.toLong())

            clips.removeAt(splitTargetIndex)
            clips.add(splitTargetIndex, newClip1)
            clips.add(splitTargetIndex + 1, newClip2)

            _uiState.update { it.copy(tracks = tracks) }
        }
    }


    fun deleteSelectedClip() {
        val selectedId = _uiState.value.selectedClipId ?: return
        val tracks = _uiState.value.tracks.toMutableList()

        for (track in tracks) {
            val iterator = track.clips.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().id == selectedId) {
                    iterator.remove()
                    break
                }
            }
        }

        _uiState.update { it.copy(tracks = tracks, selectedClipId = null) }
    }

    fun selectClip(clipId: String) {
        _uiState.update { it.copy(selectedClipId = clipId) }
    }

    fun updateCurrentTime(timeMs: Long) {
        _uiState.update { it.copy(currentTimeMs = timeMs) }
    }
}
