package com.app.douyin.pro.lib.media.network.model

import com.google.gson.annotations.SerializedName

data class SearchVideoResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("video_list") val videoList: List<VideoDto>?,
    @SerializedName("next_cursor") val nextCursor: Long,
    @SerializedName("has_more") val hasMore: Boolean
)

data class HotWordsResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("words") val words: List<String>?
)

data class SuggestResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("suggestions") val suggestions: List<String>?
)

data class SearchUserResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("user_list") val userList: List<UserDto>?,
    @SerializedName("next_cursor") val nextCursor: Long,
    @SerializedName("has_more") val hasMore: Boolean
)
