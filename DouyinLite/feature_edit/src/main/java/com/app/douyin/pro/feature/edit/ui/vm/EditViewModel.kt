package com.app.douyin.pro.feature.edit.ui.vm

import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.app.douyin.pro.feature.edit.domain.model.ClipItem
import com.app.douyin.pro.feature.edit.domain.model.EditTrack
import com.app.douyin.pro.feature.edit.domain.model.TrackType
import com.app.douyin.pro.feature.edit.ui.state.EditUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Stack
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    // 撤销重做栈
    private val undoStack = Stack<List<EditTrack>>()
    private val redoStack = Stack<List<EditTrack>>()
    private val editorHelper = com.app.douyin.pro.lib.media.VideoEditorHelper(context)

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
        clearHistory()
    }

    private fun saveHistory() {
        // 深拷贝当前轨道状态并存入栈中
        val currentTracks = _uiState.value.tracks.map { track ->
            track.copy(clips = track.clips.map { it.copy() }.toMutableList())
        }
        undoStack.push(currentTracks)
        redoStack.clear()
        updateHistoryState()
    }

    fun undo() {
        if (undoStack.isEmpty()) return

        val currentTracks = _uiState.value.tracks.map { track ->
            track.copy(clips = track.clips.map { it.copy() }.toMutableList())
        }
        redoStack.push(currentTracks)

        val previousTracks = undoStack.pop()
        _uiState.update { it.copy(tracks = previousTracks) }
        updateHistoryState()
    }

    fun redo() {
        if (redoStack.isEmpty()) return

        val currentTracks = _uiState.value.tracks.map { track ->
            track.copy(clips = track.clips.map { it.copy() }.toMutableList())
        }
        undoStack.push(currentTracks)

        val nextTracks = redoStack.pop()
        _uiState.update { it.copy(tracks = nextTracks) }
        updateHistoryState()
    }

    private fun updateHistoryState() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    private fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        updateHistoryState()
    }

    fun splitClip() {
        saveHistory()
        val currentTime = _uiState.value.currentTimeMs
        val tracks = _uiState.value.tracks.map { it.copy(clips = it.clips.toMutableList()) }

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

            val newClip1 = targetClip.copy(id = java.util.UUID.randomUUID().toString(), endInSourceMs = targetClip.startInSourceMs + splitOffsetInClip.toLong())
            val newClip2 = targetClip.copy(id = java.util.UUID.randomUUID().toString(), startInSourceMs = targetClip.startInSourceMs + splitOffsetInClip.toLong())

            clips.removeAt(splitTargetIndex)
            clips.add(splitTargetIndex, newClip1)
            clips.add(splitTargetIndex + 1, newClip2)

            _uiState.update { it.copy(tracks = tracks) }
        }
    }

    fun deleteSelectedClip() {
        saveHistory()
        val selectedId = _uiState.value.selectedClipId ?: return
        val tracks = _uiState.value.tracks.map { it.copy(clips = it.clips.toMutableList()) }

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


    fun exportProject(onSuccess: (android.net.Uri) -> Unit) {
        val timeline = com.app.douyin.pro.lib.media.model.EditingTimeline().apply {
            _uiState.value.tracks.forEach { track ->
                if (track.type == com.app.douyin.pro.feature.edit.domain.model.TrackType.VIDEO) {
                    track.clips.forEach { clip ->
                        this.videoMainTrack.add(com.app.douyin.pro.lib.media.model.VideoClip(
                            id = clip.id,
                            uri = clip.sourceUri,
                            startMs = clip.startInSourceMs,
                            endMs = clip.endInSourceMs,
                            durationMs = clip.sourceDurationMs,
                            speed = clip.speed,
                            volume = clip.volume
                        ))
                    }
                }
            }
        }

        val outputPath = java.io.File(context.cacheDir, "exported_user_video.mp4").absolutePath
        _uiState.update { it.copy(isExporting = true, exportProgress = 0) }

        editorHelper.exportTimeline(timeline, outputPath, object : com.app.douyin.pro.lib.media.VideoEditorHelper.ExportListener {
            override fun onProgress(progress: Int) {
                _uiState.update { it.copy(exportProgress = progress) }
            }

            override fun onCompleted(outputUri: android.net.Uri) {
                _uiState.update { it.copy(isExporting = false) }
                onSuccess(outputUri)
            }

            override fun onError(exception: Exception) {
                _uiState.update { it.copy(isExporting = false) }
            }
        })
    }

    fun togglePlay() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }
}
