package com.app.douyin.pro.lib.media.network.model

import com.google.gson.annotations.SerializedName

data class EffectResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("status_msg") val statusMsg: String?,
    @SerializedName("effect_list") val effectList: List<EffectDto>?
)

data class EffectDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("is_dynamic") val isDynamic: Boolean,
    @SerializedName("glsl_source") val glslSource: String?
)
