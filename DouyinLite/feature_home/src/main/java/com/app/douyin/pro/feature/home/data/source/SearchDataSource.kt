package com.app.douyin.pro.feature.home.data.source

import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.network.model.HotWordsResponse
import com.app.douyin.pro.lib.media.network.model.SearchUserResponse
import com.app.douyin.pro.lib.media.network.model.SearchVideoResponse
import com.app.douyin.pro.lib.media.network.model.SuggestResponse
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

    suspend fun getHotWords(): HotWordsResponse {
        return apiService.getHotWords()
    }

    suspend fun getSuggestions(keyword: String): SuggestResponse {
        return apiService.getSuggestions(keyword)
    }
}
