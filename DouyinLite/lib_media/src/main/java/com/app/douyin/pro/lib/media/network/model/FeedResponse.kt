package com.app.douyin.pro.lib.media.network.model

import com.google.gson.annotations.SerializedName

data class FeedResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("next_time") val nextTime: Long?,
    @SerializedName("video_list") val videoList: List<VideoDto>?
)

data class VideoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("author") val author: UserDto,
    @SerializedName("play_url") val playUrl: String,
    @SerializedName("cover_url") val coverUrl: String,
    @SerializedName("favorite_count") val favoriteCount: Long,
    @SerializedName("comment_count") val commentCount: Long,
    @SerializedName("is_favorite") val isFavorite: Boolean,
    @SerializedName("title") val title: String,
    @SerializedName("status") val status: String?,
    @SerializedName("created_at") val createdAt: Long?
)

data class UserDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("follow_count") val followCount: Long,
    @SerializedName("follower_count") val followerCount: Long,
    @SerializedName("is_follow") val isFollow: Boolean,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("signature") val signature: String?,
    @SerializedName("favorite_public") val favoritePublic: Boolean?
)
