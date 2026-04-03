import os

file_path = 'DouyinLite/feature_edit/src/main/java/com/app/douyin/pro/feature/edit/ui/vm/EditViewModel.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Add delete function
delete_func = """
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
"""

content = content.replace('    fun updateCurrentTime(timeMs: Long) {', delete_func + '\n    fun updateCurrentTime(timeMs: Long) {')

with open(file_path, 'w') as f:
    f.write(content)
