package com.app.douyin.pro.feature.home.data.source

import com.app.douyin.pro.lib.media.network.DouyinApiService
import javax.inject.Inject

interface HomeDataSource {
    suspend fun getVideos(page: Int): List<String>
}

class RemoteHomeDataSource @Inject constructor(
    private val apiService: DouyinApiService
) : HomeDataSource {

    // Cache the nextTime for pagination
    private var currentNextTime: Long? = null

    override suspend fun getVideos(page: Int): List<String> {
        val requestTime = if (page == 0) null else currentNextTime

        val response = apiService.getFeed(latestTime = requestTime)

        val list = response.videoList
        if (response.statusCode == 0 && list != null) {
            // Update cursor for next page load
            currentNextTime = response.nextTime
            return list.map { it.playUrl }
        }
        return emptyList()
    }
}

// Keep mock for fallback or preview
class MockHomeDataSource : HomeDataSource {
    override suspend fun getVideos(page: Int): List<String> {
        return if (page == 0) {
            com.app.douyin.pro.feature.home.ui.MockData.videos
        } else {
            com.app.douyin.pro.feature.home.ui.MockData.loadMoreVideos(page)
        }
    }
}
