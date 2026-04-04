package com.app.douyin.pro.feature.home.data.source

interface HomeDataSource {
    suspend fun getVideos(page: Int): List<String>
}

class MockHomeDataSource : HomeDataSource {
    override suspend fun getVideos(page: Int): List<String> {
        return if (page == 0) {
            com.app.douyin.pro.feature.home.ui.MockData.videos
        } else {
            com.app.douyin.pro.feature.home.ui.MockData.loadMoreVideos(page)
        }
    }
}
