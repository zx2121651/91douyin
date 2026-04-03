import os

file_path = 'DouyinLite/feature_edit/src/main/java/com/app/douyin/pro/feature/edit/ui/vm/EditViewModel.kt'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Add context injection for VideoEditorHelper
content = content.replace('class EditViewModel @Inject constructor()', 'class EditViewModel @Inject constructor(\n    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context\n)')

# 2. Add VideoEditorHelper initialization
content = content.replace('private val redoStack = Stack<List<EditTrack>>()',
                         'private val redoStack = Stack<List<EditTrack>>()\n    private val editorHelper = com.app.douyin.pro.lib.media.VideoEditorHelper(context)')

# 3. Add exportProject method
export_logic = """
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
"""

content = content.replace('    fun togglePlay() {', export_logic + '\n    fun togglePlay() {')

with open(file_path, 'w') as f:
    f.write(content)
