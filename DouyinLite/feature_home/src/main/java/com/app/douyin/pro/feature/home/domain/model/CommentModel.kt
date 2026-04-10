package com.app.douyin.pro.feature.home.domain.model

data class CommentModel(
    val id: Long,
    val authorName: String,
    val authorAvatar: String?,
    val content: String,
    val createDate: String,
    val replyCount: Long,
    val replies: List<CommentModel> = emptyList()
)
