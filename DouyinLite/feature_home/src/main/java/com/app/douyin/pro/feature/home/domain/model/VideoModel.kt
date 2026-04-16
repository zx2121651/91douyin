package com.app.douyin.pro.feature.home.domain.model

data class VideoModel(
    val id: Long,
    val playUrl: String,
    val coverUrl: String,
    val title: String,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String?,
    val likeCount: Long,
    val commentCount: Long,
    val shareCount: Long,
    val isLiked: Boolean,
    val isFollowed: Boolean
)
