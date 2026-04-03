package com.app.douyin.pro.feature.home.data

import com.app.douyin.pro.feature.home.ui.MockData
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor() {

    suspend fun getInitialVideos(): List<String> {
        return MockData.videos
    }

    suspend fun loadMoreVideos(page: Int): List<String> {
        delay(500) // Simulate network delay
        return MockData.loadMoreVideos(page)
    }
}
