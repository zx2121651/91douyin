package com.app.douyin.pro.feature.inbox.domain.model

data class Message(
    val id: String,
    val avatarUrl: String,
    val name: String,
    val time: String,
    val content: String,
    val isOfficial: Boolean = false,
    val hasUnreadDot: Boolean = false,
    val isLive: Boolean = false
)

data class NotificationCategory(
    val type: String,
    val title: String,
    val hasDot: Boolean = false,
    val badgeText: String? = null
)
