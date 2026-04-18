package com.app.douyin.pro.feature.inbox.domain.model

import com.app.douyin.pro.lib.media.model.UserModel

enum class NotificationType(val value: String) {
    LIKE("like"),
    COMMENT("comment"),
    FOLLOW("follow"),
    SYSTEM("system");

    companion object {
        fun fromString(type: String): NotificationType {
            return values().find { it.value == type } ?: SYSTEM
        }
    }
}

data class NotificationItem(
    val id: Long,
    val fromUser: UserModel?,
    val type: NotificationType,
    val content: String,
    val targetId: Long,
    val createTime: Long,
    val isRead: Boolean
)
