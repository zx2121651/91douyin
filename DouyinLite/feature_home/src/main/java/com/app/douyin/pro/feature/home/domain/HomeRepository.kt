package com.app.douyin.pro.feature.home.domain

import com.app.douyin.pro.feature.home.ui.MockData
import javax.inject.Inject

class HomeRepository @Inject constructor() {

    fun getInitialVideos(): List<String> {
        return MockData.videos
    }

    suspend fun loadMoreVideos(page: Int): List<String> {
        return MockData.loadMoreVideos(page)
    }
}
