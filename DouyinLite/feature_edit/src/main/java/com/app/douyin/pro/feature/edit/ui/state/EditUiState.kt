package com.app.douyin.pro.feature.edit.ui.state

import com.app.douyin.pro.feature.edit.domain.model.EditProject

data class EditUiState(
    val project: EditProject = EditProject(),
    val currentTimeMs: Long = 0L,
    val selectedClipId: String? = null,
    val isPlaying: Boolean = false,
    val isExporting: Boolean = false,
    val exportProgress: Int = 0,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
) {
    // Helper to get tracks directly if needed by UI
    val tracks get() = project.tracks
    val totalDurationMs get() = project.getTotalDurationMs()
}
