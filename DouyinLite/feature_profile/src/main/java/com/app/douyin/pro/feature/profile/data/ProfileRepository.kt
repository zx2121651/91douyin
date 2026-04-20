package com.app.douyin.pro.feature.profile.data

import com.app.douyin.pro.feature.profile.data.source.ProfileDataSource
import com.app.douyin.pro.feature.profile.data.source.ProfileInfo
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.AppError
import com.app.douyin.pro.lib.media.model.VideoModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val dataSource: ProfileDataSource
) {
    suspend fun getProfileInfo(userId: Long? = null): Resource<ProfileInfo> = try {
        Resource.Success(dataSource.getUserInfo(userId))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }

    suspend fun getPublishedVideos(userId: Long? = null, latestTime: Long? = null): Resource<Pair<List<VideoModel>, Long>> = try {
        Resource.Success(dataSource.getPublishedVideos(userId, latestTime))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }

    suspend fun getFavoriteVideos(userId: Long? = null, latestTime: Long? = null): Resource<Pair<List<VideoModel>, Long>> = try {
        Resource.Success(dataSource.getFavoriteVideos(userId, latestTime))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }

    suspend fun updateProfile(
        name: String? = null,
        signature: String? = null,
        avatarBytes: ByteArray? = null,
        backgroundBytes: ByteArray? = null,
        favoritePublic: Boolean? = null
    ): Resource<Unit> = try {
        dataSource.updateProfile(name, signature, avatarBytes, backgroundBytes, favoritePublic)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Update Failed", AppError.NetworkError)
    }
}
