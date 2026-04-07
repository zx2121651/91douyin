package com.app.douyin.pro.feature.profile.data.source

import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.auth.AuthManager
import javax.inject.Inject

interface ProfileDataSource {
    suspend fun getUserInfo(): ProfileInfo
}

class RemoteProfileDataSource @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) : ProfileDataSource {
    override suspend fun getUserInfo(): ProfileInfo {
        val userId = authManager.getUserId()
        val token = authManager.getToken()

        if (userId == -1L || token == null) {
            throw Exception("Not logged in")
        }

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
