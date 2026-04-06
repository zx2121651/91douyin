package com.app.douyin.pro.feature.inbox.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.inbox.domain.model.Message
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory
import com.app.douyin.pro.feature.inbox.domain.usecase.GetCategoriesUseCase
import com.app.douyin.pro.feature.inbox.domain.usecase.GetMessagesUseCase
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _categories = MutableStateFlow<List<NotificationCategory>>(emptyList())
    val categories: StateFlow<List<NotificationCategory>> = _categories

    init {
        viewModelScope.launch {
            val msgResult = getMessagesUseCase()
            if (msgResult is Resource.Success) _messages.value = msgResult.data
        }

        val catResult = getCategoriesUseCase()
        if (catResult is Resource.Success) _categories.value = catResult.data
    }
}
