package com.app.douyin.pro.lib.media.model

import android.net.Uri

/**
 * 视频片段模型
 */
data class VideoClip(
    val id: String,
    val uri: Uri,
    var startMs: Long,
    var endMs: Long,
    var durationMs: Long,
    var speed: Float = 1.0f,
    var volume: Float = 1.0f
)

/**
 * 音频轨道模型
 */
data class AudioTrack(
    val id: String,
    val uri: Uri,
    var timelineStartMs: Long,
    var clipStartMs: Long,
    var clipEndMs: Long,
    var volume: Float = 1.0f,
    var isMainTrack: Boolean = false
)

/**
 * 贴纸/字幕/特效基类
 */
sealed class OverlayItem {
    abstract val id: String
    abstract var timelineStartMs: Long
    abstract var durationMs: Long
}

data class TextOverlay(
    override val id: String,
    var text: String,
    var color: Int,
    override var timelineStartMs: Long,
    override var durationMs: Long,
    var positionX: Float,
    var positionY: Float
) : OverlayItem()

data class StickerOverlay(
    override val id: String,
    val stickerId: String,
    override var timelineStartMs: Long,
    override var durationMs: Long
) : OverlayItem()

/**
 * 核心时间轴模型：统筹所有轨道
 */
class EditingTimeline {
    val videoMainTrack = mutableListOf<VideoClip>()
    val pipTracks = mutableListOf<VideoClip>() // 画中画
    val audioTracks = mutableListOf<AudioTrack>()
    val overlays = mutableListOf<OverlayItem>()

    fun getTotalDurationMs(): Long {
        return videoMainTrack.sumOf { it.endMs - it.startMs }
    }
}
