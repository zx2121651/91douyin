package com.app.douyin.pro.feature.record.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.record.domain.usecase.PublishVideoUseCase
import com.app.douyin.pro.feature.record.ui.state.PublishUiState
import com.app.douyin.pro.lib.media.interaction.VideoInteractionManager
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PublishViewModel @Inject constructor(
    private val publishVideoUseCase: PublishVideoUseCase,
    private val interactionManager: VideoInteractionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishUiState())
    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

    fun setVideoUri(uri: String) {
        _uiState.update { it.copy(videoUri = uri) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun publish() {
        val currentState = _uiState.value
        if (currentState.videoUri.isEmpty()) {
            _uiState.update { it.copy(error = "Video source missing") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, error = null) }
            val uri = android.net.Uri.parse(currentState.videoUri)
            val path = if (uri.scheme == "file") uri.path else currentState.videoUri
            val videoFile = File(path ?: "")

            if (!videoFile.exists()) {
                _uiState.update { it.copy(isPublishing = false, error = "Video file not found") }
                return@launch
            }

            val result = publishVideoUseCase(videoFile, currentState.title)
            when (result) {
                is Resource.Success -> {
                    interactionManager.notifyVideoPublished()
                    _uiState.update { it.copy(isPublishing = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isPublishing = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
