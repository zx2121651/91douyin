package com.app.douyin.pro.feature.inbox.ui.vm

import androidx.lifecycle.ViewModel
import com.app.douyin.pro.feature.inbox.data.InboxRepository
import com.app.douyin.pro.feature.inbox.domain.model.Message
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val repository: InboxRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _categories = MutableStateFlow<List<NotificationCategory>>(emptyList())
    val categories: StateFlow<List<NotificationCategory>> = _categories

    init {
        _messages.value = repository.getMessages()
        _categories.value = repository.getCategories()
    }
}
