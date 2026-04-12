package com.app.douyin.pro.feature.inbox.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.inbox.domain.model.ChatMessage
import com.app.douyin.pro.feature.inbox.domain.usecase.GetChatHistoryUseCase
import com.app.douyin.pro.feature.inbox.domain.usecase.SendMessageUseCase
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private var currentToUserId: Long = -1

    fun initChat(toUserId: Long) {
        currentToUserId = toUserId

        // Initial fetch
        fetchMessages()

        // Start polling (Short-polling since no WebSocket setup)
        viewModelScope.launch {
            while (isActive) {
                delay(3000) // 3s polling for active chat
                fetchMessages()
            }
        }
    }

    private fun fetchMessages() {
        if (currentToUserId == -1L) return
        viewModelScope.launch {
            // Fetch without cursor for simplicity here (always get latest 100)
            when (val result = getChatHistoryUseCase(currentToUserId, null)) {
                is Resource.Success -> {
                    _messages.value = result.data
                }
                else -> {}
            }
        }
    }

    fun sendMessage(content: String) {
        if (currentToUserId == -1L || content.isBlank()) return
        viewModelScope.launch {
            when (sendMessageUseCase(currentToUserId, content)) {
                is Resource.Success -> {
                    // Instantly refetch or add optimistically. Refetch for simplicity.
                    fetchMessages()
                }
                else -> {}
            }
        }
    }
}
