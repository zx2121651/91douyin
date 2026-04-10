package com.app.douyin.pro.feature.inbox.domain.model

data class ChatMessage(
    val id: Long,
    val toUserId: Long,
    val fromUserId: Long,
    val content: String,
    val createTime: Long,
    val isMine: Boolean // true if fromUserId == currentUserId
)
