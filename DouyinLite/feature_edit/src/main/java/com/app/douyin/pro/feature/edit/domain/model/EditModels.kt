package com.app.douyin.pro.feature.edit.domain.model

import android.net.Uri
import java.util.UUID

/**
 * 视频片段在领域层的表示
 */
data class ClipItem(
    val id: String = UUID.randomUUID().toString(),
    val sourceUri: Uri,
    val sourceDurationMs: Long,
    var startInSourceMs: Long = 0L,
    var endInSourceMs: Long,
    var speed: Float = 1.0f,
    var volume: Float = 1.0f
) {
    fun getTimelineDurationMs(): Long = ((endInSourceMs - startInSourceMs) / speed).toLong()
}

/**
 * 轨道类型
 */
enum class TrackType {
    VIDEO,      // 主视频轨道
    PIP,        // 画中画视频轨道
    AUDIO,      // 音频轨道
    TEXT,       // 文字轨道
    STICKER     // 贴纸轨道
}

/**
 * 领域层的轨道表示
 * 可以承载不同类型的片段
 */
data class EditTrack(
    val id: String = UUID.randomUUID().toString(),
    val type: TrackType,
    val clips: MutableList<ClipItem> = mutableListOf()
)

/**
 * 领域层的项目（时间轴）表示
 */
data class EditProject(
    val id: String = UUID.randomUUID().toString(),
    val tracks: MutableList<EditTrack> = mutableListOf()
) {
    fun getMainVideoTrack(): EditTrack? = tracks.find { it.type == TrackType.VIDEO }

    fun getTotalDurationMs(): Long {
        return getMainVideoTrack()?.clips?.sumOf { it.getTimelineDurationMs() } ?: 0L
    }
}
