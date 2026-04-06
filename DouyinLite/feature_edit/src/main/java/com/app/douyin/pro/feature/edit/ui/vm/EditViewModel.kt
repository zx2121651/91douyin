package com.app.douyin.pro.feature.edit.ui.vm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.app.douyin.pro.feature.edit.domain.command.DeleteClipCommand
import com.app.douyin.pro.feature.edit.domain.command.EditCommand
import com.app.douyin.pro.feature.edit.domain.command.SplitClipCommand
import com.app.douyin.pro.feature.edit.domain.model.ClipItem
import com.app.douyin.pro.feature.edit.domain.model.EditTrack
import com.app.douyin.pro.feature.edit.domain.model.TrackType
import com.app.douyin.pro.feature.edit.ui.state.EditUiState
import com.app.douyin.pro.lib.media.api.IVideoEditor
import com.app.douyin.pro.lib.media.model.EditingTimeline
import com.app.douyin.pro.lib.media.model.VideoClip
import com.app.douyin.pro.lib.media.worker.VideoExportWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject
import com.google.gson.Gson
import com.app.douyin.pro.lib.media.model.EditingTimelineDto
import com.app.douyin.pro.lib.media.model.VideoClipDto

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

    private fun executeCommand(command: EditCommand) {
        val prevState = _uiState.value.tracks.map { it.copy(clips = it.clips.map { c -> c.copy() }.toMutableList()) }
        val nextTracks = command.execute(_uiState.value.tracks)

        if (nextTracks != _uiState.value.tracks) {
            undoStack.push(prevState)
            redoStack.clear()
            _uiState.update { it.copy(tracks = nextTracks) }
            updateHistoryState()
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentState = _uiState.value.tracks.map { it.copy(clips = it.clips.map { c -> c.copy() }.toMutableList()) }
        redoStack.push(currentState)
        _uiState.update { it.copy(tracks = undoStack.pop()) }
        updateHistoryState()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentState = _uiState.value.tracks.map { it.copy(clips = it.clips.map { c -> c.copy() }.toMutableList()) }
        undoStack.push(currentState)
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
        executeCommand(SplitClipCommand(_uiState.value.currentTimeMs))
    }

    fun deleteSelectedClip() {
        val sid = _uiState.value.selectedClipId ?: return
        executeCommand(DeleteClipCommand(sid))
        _uiState.update { it.copy(selectedClipId = null) }
    }

    fun selectClip(id: String) { _uiState.update { it.copy(selectedClipId = id) } }
    fun updateCurrentTime(t: Long) { _uiState.update { it.copy(currentTimeMs = t) } }
    fun togglePlay() { _uiState.update { it.copy(isPlaying = !it.isPlaying) } }

    fun exportProject(onSuccess: (Uri) -> Unit) {
        val videoTrack = _uiState.value.tracks.find { it.type == TrackType.VIDEO }
        if (videoTrack == null || videoTrack.clips.isEmpty()) return

        val clipDtos = videoTrack.clips.map { clip ->
            VideoClipDto(
                id = clip.id,
                uriString = clip.sourceUri.toString(),
                startMs = clip.startInSourceMs,
                endMs = clip.endInSourceMs,
                durationMs = clip.endInSourceMs - clip.startInSourceMs
            )
        }
        val timelineDto = EditingTimelineDto(videoMainTrack = clipDtos)
        val timelineJson = Gson().toJson(timelineDto)

        val outPath = File(context.cacheDir, "exported_v24_${System.currentTimeMillis()}.mp4").absolutePath

        val exportRequest = OneTimeWorkRequestBuilder<VideoExportWorker>()
            .setInputData(workDataOf(
                "timeline_json" to timelineJson,
                "output_path" to outPath
            ))
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(exportRequest)

        _uiState.update { it.copy(isExporting = true, exportProgress = 0) }

        // Observe WorkInfo via Flow (mapping LiveData to Flow for Clean Architecture consistency)
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(exportRequest.id).collect { workInfo ->
                if (workInfo == null) return@collect

                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt("progress", 0)
                        _uiState.update { it.copy(exportProgress = progress) }
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val uriStr = workInfo.outputData.getString("output_uri")
                        if (uriStr != null) {
                            _uiState.update { it.copy(isExporting = false, exportProgress = 100) }
                            onSuccess(Uri.parse(uriStr))
                        }
                    }
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        _uiState.update { it.copy(isExporting = false) }
                    }
                    else -> {}
                }
            }
        }
    }
}
