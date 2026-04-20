package com.app.douyin.pro.feature.record.ui.state

import androidx.camera.core.CameraSelector
import com.app.douyin.pro.feature.record.domain.model.FilterEffect
import com.app.douyin.pro.feature.record.domain.model.RecordSegment

enum class PermissionStatus {
    IDLE,
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}

data class RecordCapability(
    val hasCamera: Boolean = true,
    val hasMic: Boolean = true,
    val hasStorage: Boolean = true
)

data class RecordUiState(
    val lensFacing: Int = CameraSelector.LENS_FACING_FRONT,
    val isRecording: Boolean = false,
    val countdownTime: Int = 0,
    val showFilters: Boolean = false,
    val selectedFilter: FilterEffect? = FilterEffect("原片", false, null),
    val permissionStatus: PermissionStatus = PermissionStatus.IDLE,
    val capabilities: RecordCapability = RecordCapability(),
    val segments: List<RecordSegment> = emptyList(),
    val totalDurationMs: Long = 0L,
    val currentState: RecordState = RecordState.IDLE
)
