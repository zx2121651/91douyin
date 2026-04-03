package com.app.douyin.pro.feature.record.ui.state

import androidx.camera.core.CameraSelector

data class RecordUiState(
    val lensFacing: Int = CameraSelector.LENS_FACING_FRONT,
    val isRecording: Boolean = false,
    val countdownTime: Int = 0,
    val showFilters: Boolean = false,
    val selectedFilter: String = "磨皮"
)
