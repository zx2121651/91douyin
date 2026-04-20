package com.app.douyin.pro.feature.record.ui.vm

import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.record.domain.usecase.CountdownUseCase
import android.content.Context
import com.app.douyin.pro.feature.record.domain.usecase.GetAvailableFiltersUseCase
import com.app.douyin.pro.feature.record.ui.state.PermissionStatus
import com.app.douyin.pro.feature.record.ui.state.RecordCapability
import com.app.douyin.pro.feature.record.ui.state.RecordUiState
import com.app.douyin.pro.feature.record.util.RecordGuard
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val getAvailableFiltersUseCase: GetAvailableFiltersUseCase,
    private val countdownUseCase: CountdownUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private val _availableFilters = MutableStateFlow<List<com.app.douyin.pro.feature.record.domain.model.FilterEffect>>(emptyList())
    val availableFilters: StateFlow<List<com.app.douyin.pro.feature.record.domain.model.FilterEffect>> = _availableFilters.asStateFlow()

    init {
        loadFilters()
    }

    fun updatePermissionStatus(status: PermissionStatus) {
        _uiState.update { it.copy(permissionStatus = status) }
    }

    fun checkDeviceCapabilities(context: Context) {
        val hasCamera = RecordGuard.hasCameraHardware(context)
        val hasMic = RecordGuard.hasMicrophoneHardware(context)
        // Simplified storage check for now, can be expanded if needed
        val hasStorage = true

        _uiState.update {
            it.copy(
                capabilities = RecordCapability(
                    hasCamera = hasCamera,
                    hasMic = hasMic,
                    hasStorage = hasStorage
                )
            )
        }

        if (RecordGuard.isAllPermissionsGranted(context)) {
            updatePermissionStatus(PermissionStatus.GRANTED)
        }
    }

    private fun loadFilters() {
        viewModelScope.launch {
            val result = getAvailableFiltersUseCase()
            if (result is Resource.Success) {
                _availableFilters.value = result.data
            }
        }
    }

    fun toggleLens() {
        _uiState.update {
            it.copy(lensFacing = if (it.lensFacing == CameraSelector.LENS_FACING_FRONT)
                CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT)
        }
    }

    fun startCountdown(seconds: Int) {
        viewModelScope.launch {
            countdownUseCase(seconds).collect { time ->
                _uiState.update { it.copy(countdownTime = time) }
            }
        }
    }

    fun setRecording(recording: Boolean) {
        _uiState.update { it.copy(isRecording = recording) }
    }

    fun setShowFilters(show: Boolean) {
        _uiState.update { it.copy(showFilters = show) }
    }

    fun selectFilter(filter: com.app.douyin.pro.feature.record.domain.model.FilterEffect) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
