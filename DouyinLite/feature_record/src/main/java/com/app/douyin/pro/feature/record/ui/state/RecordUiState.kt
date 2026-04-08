package com.app.douyin.pro.feature.record.ui.state

import androidx.camera.core.CameraSelector
import com.app.douyin.pro.feature.record.domain.model.FilterEffect

data class RecordUiState(
    val lensFacing: Int = CameraSelector.LENS_FACING_FRONT,
    val isRecording: Boolean = false,
    val countdownTime: Int = 0,
    val showFilters: Boolean = false,
    val selectedFilter: FilterEffect? = FilterEffect("原片", false, null)
)
