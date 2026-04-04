package com.app.douyin.pro.feature.home.data

import com.app.douyin.pro.feature.home.data.source.HomeDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val remoteDataSource: HomeDataSource
) {
    suspend fun getInitialVideos(): List<String> = remoteDataSource.getVideos(0)
    suspend fun loadMoreVideos(page: Int): List<String> = remoteDataSource.getVideos(page)
}
