package com.app.douyin.pro.feature.edit.ui.vm

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.app.douyin.pro.feature.edit.data.EditorRepository
import com.app.douyin.pro.feature.edit.domain.model.ClipItem
import com.app.douyin.pro.feature.edit.domain.model.EditTrack
import com.app.douyin.pro.feature.edit.domain.model.TrackType
import com.app.douyin.pro.feature.edit.ui.state.EditUiState
import com.app.douyin.pro.lib.media.VideoEditorHelper
import com.app.douyin.pro.lib.media.model.EditingTimeline
import com.app.douyin.pro.lib.media.model.VideoClip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val exportVideoUseCase: com.app.douyin.pro.feature.edit.domain.usecase.ExportVideoUseCase,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private val undoStack = Stack<List<EditTrack>>()
    private val redoStack = Stack<List<EditTrack>>()

    fun initProject(videoUri: Uri, duration: Long) {
        val initialClip = ClipItem(sourceUri = videoUri, sourceDurationMs = duration, endInSourceMs = duration)
        val videoTrack = EditTrack(type = TrackType.VIDEO, clips = mutableListOf(initialClip))
        _uiState.update { it.copy(tracks = listOf(videoTrack), totalDurationMs = duration) }
        clearHistory()
    }

    private fun saveHistory() {
        undoStack.push(_uiState.value.tracks.map { t -> t.copy(clips = t.clips.map { it.copy() }.toMutableList()) })
        redoStack.clear()
        updateHistoryState()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.push(_uiState.value.tracks.map { t -> t.copy(clips = t.clips.map { it.copy() }.toMutableList()) })
        _uiState.update { it.copy(tracks = undoStack.pop()) }
        updateHistoryState()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.push(_uiState.value.tracks.map { t -> t.copy(clips = t.clips.map { it.copy() }.toMutableList()) })
        _uiState.update { it.copy(tracks = redoStack.pop()) }
        updateHistoryState()
    }

    private fun updateHistoryState() {
        _uiState.update { it.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
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
        var targetIdx = -1
        for (i in clips.indices) {
            val dur = clips[i].getTimelineDurationMs()
            if (currentTime > accumulatedTime && currentTime < accumulatedTime + dur) {
                targetIdx = i
                break
            }
            accumulatedTime += dur
        }

        if (targetIdx != -1) {
            val target = clips[targetIdx]
            val offset = (currentTime - accumulatedTime) * target.speed
            val c1 = target.copy(id = UUID.randomUUID().toString(), endInSourceMs = target.startInSourceMs + offset.toLong())
            val c2 = target.copy(id = UUID.randomUUID().toString(), startInSourceMs = target.startInSourceMs + offset.toLong())
            clips.removeAt(targetIdx)
            clips.add(targetIdx, c1)
            clips.add(targetIdx + 1, c2)
            _uiState.update { it.copy(tracks = tracks) }
        }
    }

    fun deleteSelectedClip() {
        saveHistory()
        val sid = _uiState.value.selectedClipId ?: return
        val tracks = _uiState.value.tracks.map { it.copy(clips = it.clips.toMutableList()) }
        tracks.forEach { it.clips.removeIf { c -> c.id == sid } }
        _uiState.update { it.copy(tracks = tracks, selectedClipId = null) }
    }

    fun selectClip(id: String) { _uiState.update { it.copy(selectedClipId = id) } }
    fun updateCurrentTime(t: Long) { _uiState.update { it.copy(currentTimeMs = t) } }
    fun togglePlay() { _uiState.update { it.copy(isPlaying = !it.isPlaying) } }

    fun exportProject(onSuccess: (Uri) -> Unit) {
        val timeline = EditingTimeline().apply {
            _uiState.value.tracks.filter { it.type == TrackType.VIDEO }.forEach { t ->
                t.clips.forEach { c ->
                    videoMainTrack.add(VideoClip(c.id, c.sourceUri, c.startInSourceMs, c.endInSourceMs, c.sourceDurationMs, c.speed, c.volume))
                }
            }
        }

        val out = File(context.cacheDir, "exported_v22.mp4").absolutePath
        _uiState.update { it.copy(isExporting = true, exportProgress = 0) }

        exportVideoUseCase(timeline, out, object : VideoEditorHelper.ExportListener {
            override fun onProgress(p: Int) { _uiState.update { it.copy(exportProgress = p) } }
            override fun onCompleted(uri: Uri) {
                _uiState.update { it.copy(isExporting = false) }
                onSuccess(uri)
            }
            override fun onError(e: Exception) { _uiState.update { it.copy(isExporting = false) } }
        })
    }
}
