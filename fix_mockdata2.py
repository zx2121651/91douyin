import sys

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/MockData.kt"

content = """package com.app.douyin.pro.feature.home.ui

import kotlinx.coroutines.delay

object MockData {
    val videos = listOf(
        "http://vjs.zencdn.net/v/oceans.mp4",
        "https://media.w3.org/2010/05/sintel/trailer.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
    )

    private val pool = listOf(
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
    )

    suspend fun loadMoreVideos(page: Int): List<String> {
        delay(800) // Simulate network delay
        return pool.map { "$it?page=$page&t=${System.currentTimeMillis()}" } // append queries to make them unique
    }
}
"""

with open(file_path, 'w') as f:
    f.write(content)
