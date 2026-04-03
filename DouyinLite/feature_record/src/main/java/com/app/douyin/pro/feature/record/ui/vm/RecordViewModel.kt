package com.app.douyin.pro.feature.record.ui.vm

import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.record.ui.state.RecordUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    fun toggleLens() {
        _uiState.update {
            it.copy(lensFacing = if (it.lensFacing == CameraSelector.LENS_FACING_FRONT)
                CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT)
        }
    }

    fun startCountdown(seconds: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(countdownTime = seconds) }
            for (i in seconds downTo 1) {
                _uiState.update { it.copy(countdownTime = i) }
                delay(1000)
            }
            _uiState.update { it.copy(countdownTime = 0) }
            // Trigger recording in UI via some effect or shared state
        }
    }

    fun setRecording(recording: Boolean) {
        _uiState.update { it.copy(isRecording = recording) }
    }

    fun setShowFilters(show: Boolean) {
        _uiState.update { it.copy(showFilters = show) }
    }

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
