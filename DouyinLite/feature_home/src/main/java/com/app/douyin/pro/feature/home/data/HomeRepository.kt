package com.app.douyin.pro.feature.home.data

import com.app.douyin.pro.feature.home.data.source.HomeDataSource
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.AppError
import com.app.douyin.pro.lib.media.network.VideoDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val remoteDataSource: HomeDataSource
) {
    suspend fun getInitialVideos(): Resource<List<VideoDto>> = try {
        Resource.Success(remoteDataSource.getVideos(0))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }

    suspend fun loadMoreVideos(page: Int): Resource<List<VideoDto>> = try {
        Resource.Success(remoteDataSource.getVideos(page))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }

    suspend fun favoriteAction(videoId: Long, actionType: Int): Resource<Boolean> = try {
        val success = remoteDataSource.favoriteAction(videoId, actionType)
        if (success) {
            Resource.Success(true)
        } else {
            Resource.Error("Favorite action failed", AppError.ServerError)
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }
}
