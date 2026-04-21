package com.app.douyin.pro.lib.media.model

import androidx.annotation.Keep

@Keep
data class TextOverlayDto(
    val id: String,
    val text: String,
    val color: Int,
    val timelineStartMs: Long,
    val durationMs: Long,
    val positionX: Float,
    val positionY: Float,
    val fontSize: Float = 16f
)

@Keep
data class StickerOverlayDto(
    val id: String,
    val stickerId: String,
    val timelineStartMs: Long,
    val durationMs: Long,
    val positionX: Float,
    val positionY: Float,
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f
)
