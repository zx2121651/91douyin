package com.app.douyin.pro.feature.inbox.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.inbox.domain.model.Conversation
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory
import com.app.douyin.pro.feature.inbox.domain.usecase.GetCategoriesUseCase
import com.app.douyin.pro.feature.inbox.domain.usecase.GetMessagesUseCase
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class InboxUiState {
    object Loading : InboxUiState()
    data class Success(
        val categories: List<NotificationCategory>,
        val conversations: List<Conversation>
    ) : InboxUiState()
    object Empty : InboxUiState()
    data class Error(val message: String) : InboxUiState()
}

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InboxUiState>(InboxUiState.Loading)
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    init {
        refresh()
        // Optional: keep polling for real-time updates if needed,
        // but for now we'll just implement a refresh mechanism.
        viewModelScope.launch {
            while (isActive) {
                delay(15000)
                silentRefresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = InboxUiState.Loading
            fetchInboxData()
        }
    }

    private suspend fun silentRefresh() {
        fetchInboxData()
    }

    private suspend fun fetchInboxData() {
        val catResult = getCategoriesUseCase()
        val msgResult = getMessagesUseCase()

        if (catResult is Resource.Success && msgResult is Resource.Success) {
            val categories = catResult.data
            val conversations = msgResult.data.sortedByDescending { it.lastTimestamp }

            if (categories.isEmpty() && conversations.isEmpty()) {
                _uiState.value = InboxUiState.Empty
            } else {
                _uiState.value = InboxUiState.Success(categories, conversations)
            }
        } else if (catResult is Resource.Error || msgResult is Resource.Error) {
            val errorMessage = when {
                catResult is Resource.Error -> catResult.message
                msgResult is Resource.Error -> msgResult.message
                else -> "Unknown Error"
            }
            // Only transition to Error state if we don't already have Success data (for silent refresh)
            if (_uiState.value !is InboxUiState.Success) {
                _uiState.value = InboxUiState.Error(errorMessage)
            }
        }
    }
}
