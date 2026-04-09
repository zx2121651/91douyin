package com.app.douyin.pro.lib.media.network.model

import com.google.gson.annotations.SerializedName

data class UnreadCountResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("unread_count") val unreadCount: Long
)

data class NotificationListResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("notification_list") val notificationList: List<NotificationDto>?
)

data class NotificationDto(
    @SerializedName("id") val id: Long,
    @SerializedName("from_user") val fromUser: UserDto?,
    @SerializedName("type") val type: String,
    @SerializedName("content") val content: String,
    @SerializedName("create_time") val createTime: Long,
    @SerializedName("is_read") val isRead: Boolean
)
