package com.app.douyin.pro.lib.media.model

data class UserModel(
    val id: Long,
    val name: String,
    val avatar: String? = null,
    val followCount: Long = 0,
    val followerCount: Long = 0,
    val isFollowed: Boolean = false,
    val signature: String? = null,
    val backgroundImage: String? = null
)
