package com.app.douyin.pro.feature.home.data.source

import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.network.VideoDto
import javax.inject.Inject

interface HomeDataSource {
    suspend fun getVideos(page: Int): List<VideoDto>
    suspend fun favoriteAction(videoId: Long, actionType: Int): Boolean
}

class RemoteHomeDataSource @Inject constructor(
    private val apiService: DouyinApiService
) : HomeDataSource {
    override suspend fun getVideos(page: Int): List<VideoDto> {
        val response = apiService.getFeed()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.status_code == 0) {
                return body.video_list
            } else {
                throw Exception(body?.status_msg ?: "Failed to get videos")
            }
        } else {
            throw Exception("HTTP Error: ${response.code()}")
        }
    }

    override suspend fun favoriteAction(videoId: Long, actionType: Int): Boolean {
        val response = apiService.favoriteAction(videoId, actionType)
        if (response.isSuccessful) {
             val body = response.body()
             return body != null && body.status_code == 0
        }
        return false
    }
}
