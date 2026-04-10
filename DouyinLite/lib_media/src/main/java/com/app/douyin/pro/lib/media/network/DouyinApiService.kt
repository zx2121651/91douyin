package com.app.douyin.pro.lib.media.network

import com.app.douyin.pro.lib.media.network.model.FeedResponse
import com.app.douyin.pro.lib.media.network.model.AuthResponse
import com.app.douyin.pro.lib.media.network.model.UserInfoResponse
import com.app.douyin.pro.lib.media.network.model.PublishResponse
import com.app.douyin.pro.lib.media.network.model.EffectResponse
import com.app.douyin.pro.lib.media.network.model.MessageChatResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface DouyinApiService {
    @GET("douyin/publish/list/")
    suspend fun getPublishList(
        @Query("user_id") userId: Long,
        @Query("token") token: String
    ): FeedResponse


    @GET("douyin/effect/list/")
    suspend fun getEffectList(): EffectResponse


    @GET("douyin/feed/")
    suspend fun getFeed(
        @Query("latest_time") latestTime: Long? = null,
        @Query("token") token: String? = null
    ): FeedResponse

    @FormUrlEncoded
    @POST("douyin/user/register/")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): AuthResponse

    @FormUrlEncoded
    @POST("douyin/user/login/")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): AuthResponse

    @GET("douyin/user/")
    suspend fun getUserInfo(
        @Query("user_id") userId: Long,
        @Query("token") token: String
    ): UserInfoResponse

    @Multipart
    @POST("douyin/publish/action/")
    suspend fun publishVideo(
        @Part("token") token: RequestBody,
        @Part("title") title: RequestBody,
        @Part data: MultipartBody.Part
    ): PublishResponse

    @POST("douyin/favorite/action/")
    suspend fun favoriteAction(
        @Query("video_id") videoId: Long,
        @Query("action_type") actionType: Int
    ): AuthResponse

    @GET("douyin/message/chat/")
    suspend fun getChatHistory(
        @Query("to_user_id") toUserId: Long,
        @Query("pre_msg_time") preMsgTime: Long? = null
    ): MessageChatResponse

    @POST("douyin/relation/action/")
    suspend fun relationAction(
        @Query("to_user_id") toUserId: Long,
        @Query("action_type") actionType: Int
    ): AuthResponse
}
