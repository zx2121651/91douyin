package com.app.douyin.pro.lib.media.model

import androidx.annotation.Keep

@Keep
data class EditingTimelineDto(
    val videoMainTrack: List<VideoClipDto>,
    val pipTracks: List<VideoClipDto> = emptyList(),
    val audioTracks: List<AudioTrackDto> = emptyList(),
    val textOverlays: List<TextOverlayDto> = emptyList(),
    val stickerOverlays: List<StickerOverlayDto> = emptyList()
)
