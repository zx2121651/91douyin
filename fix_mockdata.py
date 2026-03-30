import sys

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/MockData.kt"
with open(file_path, 'r') as f:
    content = f.read()

search_str = """package com.app.douyin.pro.feature.home.ui

object MockData {
    val videos = listOf(
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/h264/1080/Sintel_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/1080/Jellyfish_1080_10s_1MB.mp4"
    )
}"""

replace_str = """package com.app.douyin.pro.feature.home.ui

import kotlinx.coroutines.delay

object MockData {
    val videos = mutableListOf(
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/h264/1080/Sintel_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/jellyfish/mp4/h264/1080/Jellyfish_1080_10s_1MB.mp4"
    )

    private val moreVideos = listOf(
        "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_1MB.mp4",
        "https://test-videos.co.uk/vids/sintel/mp4/h264/1080/Sintel_1080_10s_1MB.mp4"
    )

    suspend fun loadMoreVideos(): List<String> {
        delay(1000) // Simulate network delay
        val newBatch = moreVideos.map { "$it?t=${System.currentTimeMillis()}" } // Make them unique for the pager
        return newBatch
    }
}"""

if search_str in content:
    content = content.replace(search_str, replace_str)
    with open(file_path, 'w') as f:
        f.write(content)
    print("MockData updated")
else:
    print("Could not find MockData string to replace")
