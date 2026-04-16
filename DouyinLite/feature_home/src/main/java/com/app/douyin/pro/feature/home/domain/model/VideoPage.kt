package com.app.douyin.pro.feature.home.domain.model

import com.app.douyin.pro.lib.media.model.VideoModel

data class VideoPage(
    val videos: List<VideoModel>,
    val nextTime: Long?
)
