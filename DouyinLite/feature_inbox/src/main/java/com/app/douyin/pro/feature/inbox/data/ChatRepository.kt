package com.app.douyin.pro.feature.inbox.data

import com.app.douyin.pro.feature.inbox.data.source.ChatDataSource
import com.app.douyin.pro.feature.inbox.domain.model.ChatMessage
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.AppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val dataSource: ChatDataSource
) {
    suspend fun getChatHistory(toUserId: Long, preMsgTime: Long? = null): Resource<List<ChatMessage>> = try {
        Resource.Success(dataSource.getChatHistory(toUserId, preMsgTime))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }

    suspend fun sendMessage(toUserId: Long, content: String): Resource<Unit> = try {
        dataSource.sendMessage(toUserId, content)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Send Failed", AppError.NetworkError)
    }
}
