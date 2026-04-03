package com.app.douyin.pro.feature.edit.ui.state

import com.app.douyin.pro.feature.edit.domain.model.EditTrack

data class EditUiState(
    val tracks: List<EditTrack> = emptyList(),
    val currentTimeMs: Long = 0L,
    val selectedClipId: String? = null,
    val isPlaying: Boolean = false,
    val isExporting: Boolean = false,
    val exportProgress: Int = 0,
    val totalDurationMs: Long = 0L,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)
