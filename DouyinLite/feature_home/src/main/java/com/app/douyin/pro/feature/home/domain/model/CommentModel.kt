package com.app.douyin.pro.feature.home.domain.model

enum class CommentStatus {
    SENDING, SUCCESS, FAILED
}

data class CommentModel(
    val id: Long,
    val authorName: String,
    val authorAvatar: String?,
    val content: String,
    val createDate: String,
    val replyCount: Long,
    val replies: List<CommentModel> = emptyList(),
    val status: CommentStatus = CommentStatus.SUCCESS,
    val tempId: String? = null
)
