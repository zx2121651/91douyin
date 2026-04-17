package com.app.douyin.pro.feature.inbox.domain.model

data class Conversation(
    val id: String,
    val avatarUrl: String,
    val name: String,
    val lastTime: String,
    val lastTimestamp: Long,
    val lastMessage: String,
    val isOfficial: Boolean = false,
    val unreadCount: Int = 0,
    val isLive: Boolean = false
)

data class NotificationCategory(
    val type: String,
    val title: String,
    val hasDot: Boolean = false,
    val badgeText: String? = null
)
