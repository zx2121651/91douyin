package com.app.douyin.pro.feature.home.data

import com.app.douyin.pro.feature.home.data.source.HomeDataSource
import com.app.douyin.pro.feature.home.data.source.MockHomeDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor() {

    // In a real app, these would be injected via DI
    private val remoteDataSource: HomeDataSource = MockHomeDataSource()

    suspend fun getInitialVideos(): List<String> {
        return remoteDataSource.getVideos(0)
    }

    suspend fun loadMoreVideos(page: Int): List<String> {
        return remoteDataSource.getVideos(page)
    }
}
