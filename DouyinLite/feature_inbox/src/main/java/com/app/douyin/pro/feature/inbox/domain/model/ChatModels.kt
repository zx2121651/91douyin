package com.app.douyin.pro.feature.inbox.domain.model

data class ChatMessage(
    val id: Long = 0,
    val localId: String = "",
    val toUserId: Long,
    val fromUserId: Long,
    val content: String,
    val createTime: Long,
    val isMine: Boolean, // true if fromUserId == currentUserId
    val status: MessageStatus = MessageStatus.SUCCESS
)

enum class MessageStatus {
    SENDING, SUCCESS, FAILED
}
