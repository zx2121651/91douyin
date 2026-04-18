package com.app.douyin.pro.feature.inbox.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.inbox.domain.model.NotificationItem
import com.app.douyin.pro.feature.inbox.domain.usecase.GetNotificationsUseCase
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationUiState {
    object Loading : NotificationUiState()
    data class Success(val notifications: List<NotificationItem>) : NotificationUiState()
    object Empty : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = getNotificationsUseCase()
            if (result is Resource.Success) {
                if (result.data.isEmpty()) {
                    _uiState.value = NotificationUiState.Empty
                } else {
                    _uiState.value = NotificationUiState.Success(result.data)
                }
            } else if (result is Resource.Error) {
                _uiState.value = NotificationUiState.Error(result.message)
            }
        }
    }
}
