package com.app.douyin.pro.feature.home.data

import com.app.douyin.pro.feature.home.data.source.HomeDataSource
import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.AppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val remoteDataSource: HomeDataSource
) {
    suspend fun getInitialVideos(): Resource<List<VideoModel>> = try { Resource.Success(remoteDataSource.getVideos(0)) } catch (e: Exception) { Resource.Error(e.message ?: "Unknown Error", AppError.UnknownError) }
    suspend fun loadMoreVideos(page: Int): Resource<List<VideoModel>> = try { Resource.Success(remoteDataSource.getVideos(page)) } catch (e: Exception) { Resource.Error(e.message ?: "Unknown Error", AppError.UnknownError) }
}
