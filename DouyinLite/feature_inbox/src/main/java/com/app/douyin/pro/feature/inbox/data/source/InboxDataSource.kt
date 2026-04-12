package com.app.douyin.pro.feature.inbox.data.source

import com.app.douyin.pro.feature.inbox.domain.model.Message
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory
import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.auth.AuthManager
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface InboxDataSource {
    suspend fun getMessages(): List<Message>
    suspend fun getCategories(): List<NotificationCategory>
}

class RemoteInboxDataSource @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) : InboxDataSource {

    override suspend fun getMessages(): List<Message> {
        try {
            val token = authManager.getToken()
            if (token == null) return emptyList()
            val response = apiService.getNotifications(token)
            if (response.statusCode == 0) {
                val format = SimpleDateFormat("MM-dd", Locale.getDefault())
                val list = response.notificationList
                if (list != null) {
                    return list.map { dto ->
                        Message(
                            id = dto.id.toString(),
                            avatarUrl = dto.fromUser?.avatar ?: "https://api.dicebear.com/7.x/avataaars/png?seed=${dto.id}",
                            name = dto.fromUser?.name ?: "系统通知",
                            time = format.format(Date(dto.createTime)),
                            content = dto.content,
                            isOfficial = dto.type == "system",
                            hasUnreadDot = !dto.isRead,
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
            val token = authManager.getToken()
            if (token != null) {
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
