package com.app.douyin.pro.lib.media.model

import androidx.annotation.Keep

@Keep
data class VideoClipDto(
    val id: String,
    val uriString: String,
    val startMs: Long,
    val endMs: Long,
    val durationMs: Long,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f
)
