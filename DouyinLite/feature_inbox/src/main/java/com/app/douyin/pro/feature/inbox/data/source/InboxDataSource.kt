package com.app.douyin.pro.feature.inbox.data.source

import com.app.douyin.pro.feature.inbox.domain.model.Conversation
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory
import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.auth.AuthManager
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface InboxDataSource {
    suspend fun getMessages(): List<Conversation>
    suspend fun getCategories(): List<NotificationCategory>
}

class RemoteInboxDataSource @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) : InboxDataSource {

    override suspend fun getMessages(): List<Conversation> {
        try {
            if (!authManager.isLoggedIn()) return emptyList()
            val token = authManager.requireToken()
            val response = apiService.getConversationList(token)
            if (response.statusCode == 0) {
                val format = SimpleDateFormat("MM-dd", Locale.getDefault())
                val list = response.conversationList
                if (list != null) {
                    return list.map { dto ->
                        Conversation(
                            id = dto.user.id.toString(),
                            avatarUrl = dto.user.avatar ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${dto.user.id}",
                            name = dto.user.name,
                            lastTime = format.format(Date(dto.createTime)),
                            lastTimestamp = dto.createTime,
                            lastMessage = dto.lastMessage,
                            isOfficial = false,
                            unreadCount = dto.unreadCount.toInt(),
                            isLive = false
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    override suspend fun getCategories(): List<NotificationCategory> {
        var unreadCount = 0L
        try {
            if (authManager.isLoggedIn()) {
                val token = authManager.requireToken()
                val response = apiService.getUnreadCount(token)
                if (response.statusCode == 0) {
                    unreadCount = response.unreadCount
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return listOf(
            NotificationCategory("group", "新朋友", false, null),
            NotificationCategory("favorite", "互动消息", unreadCount > 0, if (unreadCount > 0) unreadCount.toString() else null),
            NotificationCategory("alternate_email", "系统通知", false, null)
        )
    }
}
