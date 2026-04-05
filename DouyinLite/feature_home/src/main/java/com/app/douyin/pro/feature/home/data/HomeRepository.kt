package com.app.douyin.pro.feature.home.data

import com.app.douyin.pro.feature.home.data.source.HomeDataSource
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val remoteDataSource: HomeDataSource
) {
    suspend fun getInitialVideos(): Resource<List<String>> = try { Resource.Success(remoteDataSource.getVideos(0)) } catch (e: Exception) { Resource.Error(e.message ?: "Unknown Error") }
    suspend fun loadMoreVideos(page: Int): Resource<List<String>> = try { Resource.Success(remoteDataSource.getVideos(page)) } catch (e: Exception) { Resource.Error(e.message ?: "Unknown Error") }
}
