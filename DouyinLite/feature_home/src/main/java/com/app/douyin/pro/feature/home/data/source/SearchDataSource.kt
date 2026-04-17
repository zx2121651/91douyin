package com.app.douyin.pro.feature.home.data.source

import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.network.model.SearchUserResponse
import com.app.douyin.pro.lib.media.network.model.SearchVideoResponse
import javax.inject.Inject

class SearchDataSource @Inject constructor(
    private val apiService: DouyinApiService
) {
    suspend fun searchVideos(keyword: String, cursor: Long): SearchVideoResponse {
        return apiService.searchVideo(keyword = keyword, cursor = cursor)
    }

    suspend fun searchUsers(keyword: String, cursor: Long): SearchUserResponse {
        return apiService.searchUser(keyword = keyword, cursor = cursor)
    }
}
