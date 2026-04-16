package com.app.douyin.pro.feature.home.data.source

import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.feature.home.domain.model.VideoPage
import com.app.douyin.pro.lib.media.network.DouyinApiService
import javax.inject.Inject

interface HomeDataSource {
    suspend fun getVideos(latestTime: Long?): VideoPage
}

class RemoteHomeDataSource @Inject constructor(
    private val apiService: DouyinApiService
) : HomeDataSource {

    override suspend fun getVideos(latestTime: Long?): VideoPage {
        val response = apiService.getFeed(latestTime = latestTime)

        if (response.statusCode != 0) {
            throw Exception(response.statusMsg ?: "Server Error")
        }

        val list = response.videoList ?: return VideoPage(emptyList(), null)

        val videos = list.map { dto ->
            VideoModel(
                id = dto.id,
                playUrl = dto.playUrl,
                coverUrl = dto.coverUrl,
                title = dto.title,
                authorId = dto.author.id,
                authorName = dto.author.name,
                authorAvatar = dto.author.avatar,
                likeCount = formatCount(dto.favoriteCount),
                commentCount = formatCount(dto.commentCount),
                shareCount = "分享", // Placeholder
                isLiked = dto.isFavorite,
                isFollowed = dto.author.isFollow
            )
        }
        return VideoPage(videos, response.nextTime)
    }

    private fun formatCount(count: Long): String {
        return if (count >= 10000) {
            String.format("%.1fw", count / 10000.0)
        } else {
            count.toString()
        }
    }
}

// Keep mock for fallback or preview
class MockHomeDataSource : HomeDataSource {
    override suspend fun getVideos(latestTime: Long?): VideoPage {
        val page = if (latestTime == null) 0 else 1
        val urls = if (page == 0) {
            com.app.douyin.pro.feature.home.ui.MockData.videos
        } else {
            com.app.douyin.pro.feature.home.ui.MockData.loadMoreVideos(page)
        }
        val videos = urls.mapIndexed { index, url ->
            VideoModel(
                id = (page * 100 + index).toLong(),
                playUrl = url,
                coverUrl = "",
                title = "这是一个 Mock 视频",
                authorId = 1,
                authorName = "Mock User",
                authorAvatar = null,
                likeCount = "1.2w",
                commentCount = "856",
                shareCount = "分享",
                isLiked = false,
                isFollowed = false
            )
        }
        return VideoPage(videos, if (page == 0) System.currentTimeMillis() else null)
    }
}
