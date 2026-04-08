package com.app.douyin.pro.feature.record.ui.vm

import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.record.domain.usecase.CountdownUseCase
import com.app.douyin.pro.feature.record.domain.usecase.GetAvailableFiltersUseCase
import com.app.douyin.pro.feature.record.ui.state.RecordUiState
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

    private fun loadFilters() {
        val result = getAvailableFiltersUseCase()
        if (result is Resource.Success) {
            _availableFilters.value = result.data
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
