package com.app.douyin.pro.feature.inbox.domain.usecase

import com.app.douyin.pro.feature.inbox.data.ChatRepository
import com.app.douyin.pro.feature.inbox.domain.model.ChatMessage
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class GetChatHistoryUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(toUserId: Long, preMsgTime: Long? = null): Resource<List<ChatMessage>> = repository.getChatHistory(toUserId, preMsgTime)
}

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(toUserId: Long, content: String): Resource<Unit> = repository.sendMessage(toUserId, content)
}
