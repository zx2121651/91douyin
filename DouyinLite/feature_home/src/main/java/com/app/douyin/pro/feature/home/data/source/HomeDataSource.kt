package com.app.douyin.pro.feature.home.data.source

import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.lib.media.network.DouyinApiService
import javax.inject.Inject

interface HomeDataSource {
    suspend fun getVideos(page: Int): List<VideoModel>
}

class RemoteHomeDataSource @Inject constructor(
    private val apiService: DouyinApiService
) : HomeDataSource {

    private var currentNextTime: Long? = null

    override suspend fun getVideos(page: Int): List<VideoModel> {
        val requestTime = if (page == 0) null else currentNextTime

        try {
            val response = apiService.getFeed(latestTime = requestTime)

            val list = response.videoList
            if (response.statusCode == 0 && list != null) {
                currentNextTime = response.nextTime
                return list.map { dto ->
                    VideoModel(
                        id = dto.id,
                        playUrl = dto.playUrl,
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
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
    override suspend fun getVideos(page: Int): List<VideoModel> {
        val urls = if (page == 0) {
            com.app.douyin.pro.feature.home.ui.MockData.videos
        } else {
            com.app.douyin.pro.feature.home.ui.MockData.loadMoreVideos(page)
        }
        return urls.mapIndexed { index, url ->
            VideoModel(
                id = index.toLong(),
                playUrl = url,
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
    }
}
