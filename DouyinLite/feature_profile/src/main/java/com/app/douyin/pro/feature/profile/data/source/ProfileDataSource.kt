package com.app.douyin.pro.feature.profile.data.source

import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.model.VideoModel
import com.app.douyin.pro.lib.media.model.UserModel
import com.app.douyin.pro.lib.media.auth.AuthManager
import com.app.douyin.pro.lib.media.util.CountFormatter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

interface ProfileDataSource {
    suspend fun getUserInfo(userId: Long?): ProfileInfo
    suspend fun getPublishedVideos(userId: Long?, latestTime: Long?): Pair<List<VideoModel>, Long>
    suspend fun getFavoriteVideos(userId: Long?, latestTime: Long?): Pair<List<VideoModel>, Long>
    suspend fun updateProfile(
        name: String? = null,
        signature: String? = null,
        avatarBytes: ByteArray? = null,
        backgroundBytes: ByteArray? = null,
        favoritePublic: Boolean? = null
    )
}

class RemoteProfileDataSource @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) : ProfileDataSource {
    override suspend fun getUserInfo(userId: Long?): ProfileInfo {
        val token = authManager.requireToken()
        val targetUserId = userId ?: authManager.getUserId()

        val response = apiService.getUserInfo(targetUserId, token)
        val user = response.user

        if (response.statusCode == 0 && user != null) {
            return ProfileInfo(
                id = user.id,
                username = user.name.takeIf { it.isNotEmpty() } ?: "User_$targetUserId",
                douyinId = "dy_${user.id}",
                following = user.followCount.toInt(),
                followers = CountFormatter.format(user.followerCount),
                followerCount = user.followerCount,
                isFollowed = user.isFollow,
                likes = "0", // Currently we don't return total likes received, default to 0
                avatar = user.avatar,
                backgroundImage = user.backgroundImage,
                signature = user.signature,
                favoritePublic = user.favoritePublic ?: false,
                workCount = 0, // Will be updated by ViewModel or separate call
                favoritedCount = 0
            )
        }
        throw Exception(response.statusMsg ?: "Failed to load profile")
    }

    override suspend fun getFavoriteVideos(userId: Long?, latestTime: Long?): Pair<List<VideoModel>, Long> {
        val targetUserId = userId ?: authManager.getUserId()
        val token = authManager.getToken()

        try {
            val response = apiService.getFavoriteList(targetUserId, token)
            if (response.statusCode == 0) {
                val videos = response.videoList?.map { dto ->
                    VideoModel(
                        id = dto.id,
                        playUrl = dto.playUrl,
                        coverUrl = dto.coverUrl,
                        title = dto.title,
                        author = UserModel(
                            id = dto.author.id,
                            name = dto.author.name,
                            avatar = dto.author.avatar,
                            followCount = dto.author.followCount,
                            followerCount = dto.author.followerCount,
                            isFollowed = dto.author.isFollow,
                            signature = dto.author.signature,
                            backgroundImage = dto.author.backgroundImage
                        ),
                        likeCount = dto.favoriteCount,
                        commentCount = dto.commentCount,
                        isLiked = dto.isFavorite,
                        status = dto.status ?: "published",
                        createdAt = dto.createdAt ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                return videos to (response.nextTime ?: 0L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList<VideoModel>() to 0L
    }

    override suspend fun getPublishedVideos(userId: Long?, latestTime: Long?): Pair<List<VideoModel>, Long> {
        if (!authManager.isLoggedIn()) return emptyList<VideoModel>() to 0L
        val targetUserId = userId ?: authManager.getUserId()
        val token = authManager.requireToken()

        try {
            val response = apiService.getPublishList(targetUserId, token, latestTime)
            if (response.statusCode == 0) {
                val videos = response.videoList?.map { dto ->
                    VideoModel(
                        id = dto.id,
                        playUrl = dto.playUrl,
                        coverUrl = dto.coverUrl,
                        title = dto.title,
                        author = UserModel(
                            id = dto.author.id,
                            name = dto.author.name,
                            avatar = dto.author.avatar,
                            followCount = dto.author.followCount,
                            followerCount = dto.author.followerCount,
                            isFollowed = dto.author.isFollow,
                            signature = dto.author.signature,
                            backgroundImage = dto.author.backgroundImage
                        ),
                        likeCount = dto.favoriteCount,
                        commentCount = dto.commentCount,
                        isLiked = dto.isFavorite,
                        status = dto.status ?: "published",
                        createdAt = dto.createdAt ?: System.currentTimeMillis()
                    )
                } ?: emptyList()
                return videos to (response.nextTime ?: 0L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList<VideoModel>() to 0L
    }

    override suspend fun updateProfile(
        name: String?,
        signature: String?,
        avatarBytes: ByteArray?,
        backgroundBytes: ByteArray?,
        favoritePublic: Boolean?
    ) {
        val token = authManager.requireToken()
        val tokenBody = token.toRequestBody("text/plain".toMediaTypeOrNull())
        val nameBody = name?.toRequestBody("text/plain".toMediaTypeOrNull())
        val signatureBody = signature?.toRequestBody("text/plain".toMediaTypeOrNull())
        val favoritePublicBody = favoritePublic?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        val avatarPart = avatarBytes?.let {
            val requestFile = it.toRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestFile)
        }

        val backgroundPart = backgroundBytes?.let {
            val requestFile = it.toRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("background", "background.jpg", requestFile)
        }

        val response = apiService.updateProfile(
            token = tokenBody,
            name = nameBody,
            signature = signatureBody,
            avatar = avatarPart,
            background = backgroundPart,
            favoritePublic = favoritePublicBody
        )

        if (response.statusCode != 0) {
            throw Exception(response.statusMsg ?: "Failed to update profile")
        }
    }

}

data class ProfileInfo(
    val id: Long,
    val username: String,
    val douyinId: String,
    val following: Int,
    val followers: String,
    val followerCount: Long,
    val isFollowed: Boolean,
    val likes: String,
    val avatar: String? = null,
    val backgroundImage: String? = null,
    val signature: String? = null,
    val favoritePublic: Boolean = false,
    val workCount: Int = 0,
    val favoritedCount: Long = 0
)
