package com.app.douyin.pro.lib.media.model

import androidx.annotation.Keep

@Keep
data class AudioTrackDto(
    val id: String,
    val uriString: String,
    val timelineStartMs: Long,
    val startInSourceMs: Long,
    val endInSourceMs: Long,
    val sourceDurationMs: Long,
    val volume: Float = 1.0f,
    val speed: Float = 1.0f,
    val isMainTrack: Boolean = false
)
