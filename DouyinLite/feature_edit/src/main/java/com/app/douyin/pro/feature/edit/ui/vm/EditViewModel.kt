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
import com.app.douyin.pro.feature.edit.domain.command.ChangeSpeedCommand
import com.app.douyin.pro.feature.edit.domain.command.ChangeVolumeCommand
import com.app.douyin.pro.feature.edit.domain.model.ClipItem
import com.app.douyin.pro.feature.edit.domain.model.EditTrack
import com.app.douyin.pro.feature.edit.domain.model.EditProject
import com.app.douyin.pro.feature.edit.domain.model.TrackType
import com.app.douyin.pro.feature.edit.ui.state.EditUiState
import com.app.douyin.pro.lib.media.MediaAssetManager
import com.app.douyin.pro.lib.media.worker.VideoExportWorker
import com.app.douyin.pro.lib.media.model.EditingTimelineDto
import com.app.douyin.pro.lib.media.model.VideoClipDto
import com.app.douyin.pro.lib.media.model.AudioTrackDto
import com.app.douyin.pro.lib.media.model.TextOverlayDto
import com.app.douyin.pro.lib.media.model.StickerOverlayDto
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

@HiltViewModel
class EditViewModel @Inject constructor(
    private val exportVideoUseCase: com.app.douyin.pro.feature.edit.domain.usecase.ExportVideoUseCase,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val mediaAssetManager: MediaAssetManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUiState())
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    private val undoStack = Stack<List<EditTrack>>()
    private val redoStack = Stack<List<EditTrack>>()

    fun initProject(videoUri: Uri, duration: Long) {
        val initialClip = ClipItem(
            sourceUri = videoUri,
            sourceDurationMs = duration,
            endInSourceMs = duration
        )
        val videoTrack = EditTrack(type = TrackType.VIDEO, clips = mutableListOf(initialClip))
        val project = EditProject(tracks = mutableListOf(videoTrack))
        _uiState.update { it.copy(project = project) }
        clearHistory()
    }

    fun initProjectWithSegments(segmentsJson: String) {
        try {
            val type = object : com.google.gson.reflect.TypeToken<List<com.app.douyin.pro.feature.record.domain.model.RecordSegment>>() {}.type
            val segments: List<com.app.douyin.pro.feature.record.domain.model.RecordSegment> = Gson().fromJson(segmentsJson, type)

            val clips = segments.map { segment ->
                ClipItem(
                    sourceUri = Uri.fromFile(File(segment.filePath)),
                    sourceDurationMs = segment.durationMs,
                    endInSourceMs = segment.durationMs
                )
            }.toMutableList()

            val videoTrack = EditTrack(type = TrackType.VIDEO, clips = clips)
            val project = EditProject(tracks = mutableListOf(videoTrack))

            _uiState.update { it.copy(project = project) }
            clearHistory()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun executeCommand(command: EditCommand) {
        val currentTracks = _uiState.value.project.tracks
        val prevState = currentTracks.map { track ->
            track.copy(clips = track.clips.map { it.copy() }.toMutableList())
        }
        val nextTracks = command.execute(currentTracks)

        if (nextTracks != currentTracks) {
            undoStack.push(prevState)
            redoStack.clear()
            _uiState.update { it.copy(project = it.project.copy(tracks = nextTracks.toMutableList())) }
            updateHistoryState()
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentTracks = _uiState.value.project.tracks
        val currentState = currentTracks.map { track ->
            track.copy(clips = track.clips.map { it.copy() }.toMutableList())
        }
        redoStack.push(currentState)
        val prevTracks = undoStack.pop()
        _uiState.update { it.copy(project = it.project.copy(tracks = prevTracks.toMutableList())) }
        updateHistoryState()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentTracks = _uiState.value.project.tracks
        val currentState = currentTracks.map { track ->
            track.copy(clips = track.clips.map { it.copy() }.toMutableList())
        }
        undoStack.push(currentState)
        val nextTracks = redoStack.pop()
        _uiState.update { it.copy(project = it.project.copy(tracks = nextTracks.toMutableList())) }
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

    fun changeSelectedClipSpeed(speed: Float) {
        val sid = _uiState.value.selectedClipId ?: return
        executeCommand(ChangeSpeedCommand(sid, speed))
    }

    fun changeSelectedClipVolume(volume: Float) {
        val sid = _uiState.value.selectedClipId ?: return
        executeCommand(ChangeVolumeCommand(sid, volume))
    }

    fun updateClipBoundaries(clipId: String, startMs: Long, endMs: Long) {
        val project = _uiState.value.project
        val newTracks = project.tracks.map { track ->
            val newClips = track.clips.map { clip ->
                if (clip.id == clipId) {
                    val validStart = startMs.coerceIn(0L, clip.sourceDurationMs - 100L)
                    val validEnd = endMs.coerceIn(validStart + 100L, clip.sourceDurationMs)
                    clip.copy(startInSourceMs = validStart, endInSourceMs = validEnd)
                } else {
                    clip
                }
            }.toMutableList()
            track.copy(clips = newClips)
        }
        _uiState.update { it.copy(project = project.copy(tracks = newTracks.toMutableList())) }
    }

    fun setCoverTimestamp(timestampMs: Long) {
        _uiState.update { it.copy(project = it.project.copy(coverTimestampMs = timestampMs)) }
    }

    fun selectClip(id: String) { _uiState.update { it.copy(selectedClipId = id) } }
    fun updateCurrentTime(t: Long) { _uiState.update { it.copy(currentTimeMs = t) } }
    fun togglePlay() { _uiState.update { it.copy(isPlaying = !it.isPlaying) } }

    fun exportProject(onSuccess: (Uri, Long) -> Unit) {
        val project = _uiState.value.project
        val videoTrack = project.tracks.find { it.type == TrackType.VIDEO }
        if (videoTrack == null || videoTrack.clips.isEmpty()) return
        if (project.getTotalDurationMs() <= 0) return

        // Mapping domain models to DTOs
        val clipDtos = videoTrack.clips.map { clip ->
            VideoClipDto(
                id = clip.id,
                uriString = clip.sourceUri.toString(),
                startInSourceMs = clip.startInSourceMs,
                endInSourceMs = clip.endInSourceMs,
                sourceDurationMs = clip.sourceDurationMs,
                speed = clip.speed,
                volume = clip.volume
            )
        }

        val pipTrack = project.tracks.find { it.type == TrackType.PIP }
        val pipClipDtos = pipTrack?.clips?.map { clip ->
            VideoClipDto(
                id = clip.id,
                uriString = clip.sourceUri.toString(),
                startInSourceMs = clip.startInSourceMs,
                endInSourceMs = clip.endInSourceMs,
                sourceDurationMs = clip.sourceDurationMs,
                speed = clip.speed,
                volume = clip.volume
            )
        } ?: emptyList()

        // Placeholder for other tracks (Audio, Text, Sticker)
        // In a real scenario, we would map them here as well.

        val timelineDto = EditingTimelineDto(
            videoMainTrack = clipDtos,
            pipTracks = pipClipDtos,
            coverTimestampMs = project.coverTimestampMs
        )
        val timelineJson = Gson().toJson(timelineDto)

        val outPath = mediaAssetManager.getNewExportPath()

        val exportRequest = OneTimeWorkRequestBuilder<VideoExportWorker>()
            .setInputData(workDataOf(
                "timeline_json" to timelineJson,
                "output_path" to outPath
            ))
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(exportRequest)

        _uiState.update { it.copy(isExporting = true, exportProgress = 0) }

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
                            onSuccess(Uri.parse(uriStr), project.coverTimestampMs)
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
