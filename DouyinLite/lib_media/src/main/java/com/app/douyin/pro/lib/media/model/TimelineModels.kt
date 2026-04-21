package com.app.douyin.pro.lib.media.model

import android.net.Uri

/**
 * 视频片段模型
 * 表示一段视频素材及其在时间轴上的剪辑范围
 */
data class VideoClip(
    val id: String,
    val uri: Uri,
    var startInSourceMs: Long, // 在源文件中的起始时间
    var endInSourceMs: Long,   // 在源文件中的结束时间
    var sourceDurationMs: Long, // 源文件总时长
    var speed: Float = 1.0f,
    var volume: Float = 1.0f
) {
    /**
     * 该片段在时间轴上占据的时长
     */
    fun getTimelineDurationMs(): Long {
        return ((endInSourceMs - startInSourceMs) / speed).toLong()
    }
}

/**
 * 音频轨道模型
 */
data class AudioTrack(
    val id: String,
    val uri: Uri,
    var timelineStartMs: Long, // 在时间轴上的起始位置
    var startInSourceMs: Long, // 在源文件中的起始时间
    var endInSourceMs: Long,   // 在源文件中的结束时间
    var sourceDurationMs: Long, // 源文件总时长
    var volume: Float = 1.0f,
    var speed: Float = 1.0f,
    var isMainTrack: Boolean = false
) {
    fun getTimelineDurationMs(): Long {
        return ((endInSourceMs - startInSourceMs) / speed).toLong()
    }
}

/**
 * 贴纸/字幕/特效基类
 */
sealed class OverlayItem {
    abstract val id: String
    abstract var timelineStartMs: Long // 在时间轴上的起始位置
    abstract var durationMs: Long      // 持续时长
}

data class TextOverlay(
    override val id: String,
    var text: String,
    var color: Int,
    override var timelineStartMs: Long,
    override var durationMs: Long,
    var positionX: Float,
    var positionY: Float,
    var fontSize: Float = 16f
) : OverlayItem()

data class StickerOverlay(
    override val id: String,
    val stickerId: String,
    override var timelineStartMs: Long,
    override var durationMs: Long,
    var positionX: Float,
    var positionY: Float,
    var scale: Float = 1.0f,
    var rotation: Float = 0.0f
) : OverlayItem()

/**
 * 核心时间轴模型：统筹所有轨道
 */
class EditingTimeline {
    // 主视频轨道（顺序排列）
    val videoMainTrack = mutableListOf<VideoClip>()
    // 画中画轨道
    val pipTracks = mutableListOf<VideoClip>()
    // 音频轨道
    val audioTracks = mutableListOf<AudioTrack>()
    // 覆盖层轨道（字幕、贴纸等）
    val overlays = mutableListOf<OverlayItem>()

    /**
     * 获取总时长（以主视频轨道为准）
     */
    fun getTotalDurationMs(): Long {
        return videoMainTrack.sumOf { it.getTimelineDurationMs() }
    }
}
