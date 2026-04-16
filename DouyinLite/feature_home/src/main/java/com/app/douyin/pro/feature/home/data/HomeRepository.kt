package com.app.douyin.pro.feature.home.data

import com.app.douyin.pro.feature.home.data.source.HomeDataSource
import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.feature.home.domain.model.VideoPage
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.AppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val remoteDataSource: HomeDataSource
) {
    suspend fun getInitialVideos(): Resource<VideoPage> = safeApiCall { remoteDataSource.getVideos(null) }
    suspend fun loadMoreVideos(latestTime: Long?): Resource<VideoPage> = safeApiCall { remoteDataSource.getVideos(latestTime) }

    private suspend fun <T> safeApiCall(call: suspend () -> T): Resource<T> {
        return try {
            Resource.Success(call())
        } catch (e: java.io.IOException) {
            Resource.Error("网络连接错误，请检查网络设置", AppError.NetworkError)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "服务器响应异常", AppError.ServerError)
        }
    }
}
