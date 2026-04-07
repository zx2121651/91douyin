package com.app.douyin.pro.lib.media.network.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("token") val token: String
)

data class UserInfoResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("user") val user: UserDto?
)

data class PublishResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?
)
