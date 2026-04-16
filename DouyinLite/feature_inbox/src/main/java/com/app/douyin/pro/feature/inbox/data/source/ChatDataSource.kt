package com.app.douyin.pro.feature.inbox.data.source

import com.app.douyin.pro.feature.inbox.domain.model.ChatMessage
import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.auth.AuthManager
import javax.inject.Inject

interface ChatDataSource {
    suspend fun getChatHistory(toUserId: Long, preMsgTime: Long?): List<ChatMessage>
    suspend fun sendMessage(toUserId: Long, content: String)
}

class RemoteChatDataSource @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) : ChatDataSource {

    override suspend fun getChatHistory(toUserId: Long, preMsgTime: Long?): List<ChatMessage> {
        if (!authManager.isLoggedIn()) return emptyList()
        val token = authManager.requireToken()
        val currentUserId = authManager.getUserId()

        try {
            val response = apiService.getChatHistory(toUserId, preMsgTime, token)
            if (response.statusCode == 0) {
                response.messageList?.let { list ->
                    return list.map { dto ->
                        ChatMessage(
                            id = dto.id,
                            toUserId = dto.toUserId,
                            fromUserId = dto.fromUserId,
                            content = dto.content,
                            createTime = dto.createTime,
                            isMine = dto.fromUserId == currentUserId
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    override suspend fun sendMessage(toUserId: Long, content: String) {
        val token = authManager.requireToken()
        val response = apiService.sendMessage(toUserId = toUserId, content = content, token = token)
        if (response.statusCode != 0) {
            throw Exception(response.statusMsg ?: "Failed to send message")
        }
    }
}
