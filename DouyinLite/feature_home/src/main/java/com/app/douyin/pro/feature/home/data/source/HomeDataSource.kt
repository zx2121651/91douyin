package com.app.douyin.pro.feature.home.data.source

import com.app.douyin.pro.feature.home.domain.model.VideoPage
import com.app.douyin.pro.lib.media.model.UserModel
import com.app.douyin.pro.lib.media.model.VideoModel
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
                author = UserModel(
                    id = dto.author.id,
                    name = dto.author.name,
                    avatar = dto.author.avatar,
                    followCount = dto.author.followCount,
                    followerCount = dto.author.followerCount,
                    isFollowed = dto.author.isFollow,
                    signature = dto.author.signature,
                    backgroundImage = dto.author.backgroundImage
                ),
                likeCount = dto.favoriteCount,
                commentCount = dto.commentCount,
                shareCount = 0L, // Placeholder
                isLiked = dto.isFavorite,
                status = dto.status ?: "published",
                createdAt = dto.createdAt ?: System.currentTimeMillis()
            )
        }
        return VideoPage(videos, response.nextTime)
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
                author = UserModel(
                    id = 1,
                    name = "Mock User",
                    avatar = null
                ),
                likeCount = 12000L,
                commentCount = 856L,
                shareCount = 0L,
                isLiked = false
            )
        }
        return VideoPage(videos, if (page == 0) System.currentTimeMillis() else null)
    }
}
