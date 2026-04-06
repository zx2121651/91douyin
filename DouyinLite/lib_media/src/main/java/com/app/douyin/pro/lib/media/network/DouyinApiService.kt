package com.app.douyin.pro.lib.media.network

import com.app.douyin.pro.lib.media.network.model.FeedResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DouyinApiService {
    @GET("douyin/feed/")
    suspend fun getFeed(
        @Query("latest_time") latestTime: Long? = null,
        @Query("token") token: String? = null
    ): FeedResponse
}
