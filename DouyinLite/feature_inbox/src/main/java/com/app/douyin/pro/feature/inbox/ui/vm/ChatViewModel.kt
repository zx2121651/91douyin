package com.app.douyin.pro.feature.inbox.ui.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.inbox.domain.model.ChatMessage
import com.app.douyin.pro.feature.inbox.domain.model.MessageStatus
import com.app.douyin.pro.feature.inbox.domain.usecase.GetChatHistoryUseCase
import com.app.douyin.pro.feature.inbox.domain.usecase.SendMessageUseCase
import com.app.douyin.pro.lib.media.auth.AuthManager
import com.app.douyin.pro.lib.media.interaction.MessageUnreadManager
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val authManager: AuthManager,
    private val unreadManager: MessageUnreadManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val currentToUserId: Long = savedStateHandle.get<Long>("userId") ?: -1L
    val userName: String = savedStateHandle.get<String>("userName") ?: "User"

    init {
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
        val lastSuccessfulMsgTime = _messages.value
            .filter { it.status == MessageStatus.SUCCESS && it.id != 0L }
            .maxByOrNull { it.createTime }?.createTime ?: 0L

        viewModelScope.launch {
            when (val result = getChatHistoryUseCase(currentToUserId, lastSuccessfulMsgTime)) {
                is Resource.Success -> {
                    mergeMessages(result.data)
                    if (result.data.isNotEmpty()) {
                        unreadManager.refreshUnreadCount()
                    }
                }
                else -> {}
            }
        }
    }

    private fun mergeMessages(newMessages: List<ChatMessage>) {
        if (newMessages.isEmpty()) return
        _messages.update { currentList ->
            val merged = currentList.toMutableList()
            newMessages.forEach { newMsg ->
                // Check if this server message matches an optimistic message
                // Since we don't have localId on server, we match by content and sender/receiver and time proximity
                val optimisticMatch = merged.find {
                    it.status != MessageStatus.SUCCESS &&
                            it.content == newMsg.content &&
                            it.fromUserId == newMsg.fromUserId &&
                            Math.abs(it.createTime - newMsg.createTime) < 10000 // 10s window
                }

                if (optimisticMatch != null) {
                    // Replace optimistic message with server message
                    val index = merged.indexOf(optimisticMatch)
                    merged[index] = newMsg
                } else if (merged.none { it.id == newMsg.id }) {
                    // Add if not already present
                    merged.add(newMsg)
                }
            }
            merged.sortedBy { it.createTime }
        }
    }

    fun retrySendMessage(localId: String) {
        val message = _messages.value.find { it.localId == localId } ?: return
        if (message.status != MessageStatus.FAILED) return

        _messages.update { list ->
            list.map {
                if (it.localId == localId) it.copy(status = MessageStatus.SENDING) else it
            }
        }

        viewModelScope.launch {
            when (sendMessageUseCase(currentToUserId, message.content)) {
                is Resource.Success -> {
                    _messages.update { list ->
                        list.map {
                            if (it.localId == localId) it.copy(status = MessageStatus.SUCCESS) else it
                        }
                    }
                }
                is Resource.Error -> {
                    _messages.update { list ->
                        list.map {
                            if (it.localId == localId) it.copy(status = MessageStatus.FAILED) else it
                        }
                    }
                }
                else -> {}
            }
        }
    }

    fun sendMessage(content: String) {
        if (currentToUserId == -1L || content.isBlank()) return

        val localId = UUID.randomUUID().toString()
        val currentUserId = authManager.getUserId()
        val optimisticMsg = ChatMessage(
            localId = localId,
            toUserId = currentToUserId,
            fromUserId = currentUserId,
            content = content,
            createTime = System.currentTimeMillis(),
            isMine = true,
            status = MessageStatus.SENDING
        )

        _messages.update { it + optimisticMsg }

        viewModelScope.launch {
            when (sendMessageUseCase(currentToUserId, content)) {
                is Resource.Success -> {
                    _messages.update { list ->
                        list.map {
                            if (it.localId == localId) it.copy(status = MessageStatus.SUCCESS) else it
                        }
                    }
                }
                is Resource.Error -> {
                    _messages.update { list ->
                        list.map {
                            if (it.localId == localId) it.copy(status = MessageStatus.FAILED) else it
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
