package com.app.douyin.pro.feature.edit.domain.model

import android.net.Uri
import java.util.UUID

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

enum class TrackType { VIDEO, AUDIO, TEXT }

data class EditTrack(
    val id: String = UUID.randomUUID().toString(),
    val type: TrackType,
    val clips: MutableList<ClipItem> = mutableListOf()
)

data class EditProject(
    val id: String = UUID.randomUUID().toString(),
    val tracks: MutableList<EditTrack> = mutableListOf()
)
