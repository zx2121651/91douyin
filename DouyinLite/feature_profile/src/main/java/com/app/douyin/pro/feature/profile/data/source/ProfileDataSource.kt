package com.app.douyin.pro.feature.profile.data.source

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileDataSource @Inject constructor() {
    fun getUserInfo() = ProfileInfo(
        username = "南京最帅程序员",
        douyinId = "JulesCode_99",
        following = 128,
        followers = "2.5w",
        likes = "10.2w"
    )
}

data class ProfileInfo(
    val username: String,
    val douyinId: String,
    val following: Int,
    val followers: String,
    val likes: String
)
