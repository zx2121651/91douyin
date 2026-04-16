package com.app.douyin.pro.feature.home.domain.model

data class VideoPage(
    val videos: List<VideoModel>,
    val nextTime: Long?
)
