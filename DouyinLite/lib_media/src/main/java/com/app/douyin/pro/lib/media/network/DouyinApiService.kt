package com.app.douyin.pro.lib.media.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DouyinApiService {
    @GET("douyin/feed/")
    suspend fun getFeed(): Response<FeedResponse>

    @POST("douyin/favorite/action/")
    suspend fun favoriteAction(
        @Query("video_id") videoId: Long,
        @Query("action_type") actionType: Int
    ): Response<BaseResponse>
}

data class BaseResponse(
    val status_code: Int,
    val status_msg: String?
)

data class FeedResponse(
    val status_code: Int,
    val status_msg: String?,
    val video_list: List<VideoDto>
)

data class VideoDto(
    val id: Long,
    val play_url: String,
    val cover_url: String,
    val favorite_count: Long,
    val comment_count: Long,
    val is_favorite: Boolean,
    val title: String,
    val author: UserDto
)

data class UserDto(
    val id: Long,
    val name: String,
    val avatar: String,
    val is_follow: Boolean
)
