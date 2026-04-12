package com.app.douyin.pro.lib.media.network.model

import com.google.gson.annotations.SerializedName

data class CommentActionResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("comment") val comment: CommentDto?
)

data class CommentListResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("comment_list") val commentList: List<CommentDto>?
)

data class CommentDto(
    @SerializedName("id") val id: Long,
    @SerializedName("user") val user: UserDto,
    @SerializedName("content") val content: String,
    @SerializedName("create_date") val createDate: String,
    @SerializedName("reply_count") val replyCount: Long,
    @SerializedName("replies") val replies: List<CommentDto>?
)
