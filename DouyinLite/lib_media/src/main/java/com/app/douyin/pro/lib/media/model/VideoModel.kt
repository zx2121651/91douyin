package com.app.douyin.pro.lib.media.model

data class VideoModel(
    val id: Long,
    val author: UserModel,
    val playUrl: String,
    val coverUrl: String,
    val title: String,
    val status: String = "published",
    val createdAt: Long = System.currentTimeMillis(),
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val shareCount: Long = 0,
    val isLiked: Boolean = false,
    val categoryTag: String? = null
)
