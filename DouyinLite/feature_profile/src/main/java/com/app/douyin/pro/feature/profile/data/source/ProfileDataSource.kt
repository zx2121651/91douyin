package com.app.douyin.pro.feature.profile.data.source

import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.network.model.VideoDto
import com.app.douyin.pro.lib.media.auth.AuthManager
import javax.inject.Inject

interface ProfileDataSource {
    suspend fun getUserInfo(): ProfileInfo
    suspend fun getPublishedVideos(): List<VideoDto>
}

class RemoteProfileDataSource @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) : ProfileDataSource {
    override suspend fun getUserInfo(): ProfileInfo {
        val token = authManager.requireToken()
        val userId = authManager.getUserId()

        val response = apiService.getUserInfo(userId, token)
        val user = response.user

        if (response.statusCode == 0 && user != null) {
            return ProfileInfo(
                username = user.name.takeIf { it.isNotEmpty() } ?: "User_$userId",
                douyinId = "dy_$userId",
                following = user.followCount.toInt(),
                followers = formatCount(user.followerCount),
                likes = "0", // Currently we don't return total likes received, default to 0
                avatar = user.avatar,
                backgroundImage = user.backgroundImage,
                signature = user.signature
            )
        }
        throw Exception(response.statusMsg ?: "Failed to load profile")
    }

    override suspend fun getPublishedVideos(): List<VideoDto> {
        if (!authManager.isLoggedIn()) return emptyList()
        val userId = authManager.getUserId()
        val token = authManager.requireToken()

        try {
            val response = apiService.getPublishList(userId, token)
            if (response.statusCode == 0) {
                response.videoList?.let { list ->
                    return list
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    private fun formatCount(count: Long): String {
        return if (count >= 10000) {
            String.format("%.1fw", count / 10000.0)
        } else {
            count.toString()
        }
    }
}

data class ProfileInfo(
    val username: String,
    val douyinId: String,
    val following: Int,
    val followers: String,
    val likes: String,
    val avatar: String? = null,
    val backgroundImage: String? = null,
    val signature: String? = null
)
