package com.app.douyin.pro.lib.media.network.model

import com.google.gson.annotations.SerializedName

data class MessageChatResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("message_list") val messageList: List<ChatMessageDto>?
)

data class MessageActionResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?
)

data class ChatMessageDto(
    @SerializedName("id") val id: Long,
    @SerializedName("to_user_id") val toUserId: Long,
    @SerializedName("from_user_id") val fromUserId: Long,
    @SerializedName("content") val content: String,
    @SerializedName("create_time") val createTime: Long
)

data class ConversationListResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("conversation_list") val conversationList: List<ConversationDto>?
)

data class ConversationDto(
    @SerializedName("user") val user: UserDto,
    @SerializedName("last_message") val lastMessage: String,
    @SerializedName("create_time") val createTime: Long,
    @SerializedName("unread_count") val unreadCount: Long
)
