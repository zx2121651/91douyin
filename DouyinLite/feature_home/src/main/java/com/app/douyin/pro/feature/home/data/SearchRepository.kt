package com.app.douyin.pro.feature.home.data

import com.app.douyin.pro.feature.home.data.source.SearchDataSource
import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.lib.media.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val remoteDataSource: SearchDataSource
) {
    suspend fun searchVideos(keyword: String, cursor: Long): Resource<Pair<List<VideoModel>, Long>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = remoteDataSource.searchVideos(keyword, cursor)
                if (response.statusCode == 0) {
                    val models = response.videoList?.map { dto ->
                        VideoModel(
                            id = dto.id,
                            playUrl = dto.playUrl,
                            coverUrl = dto.coverUrl,
                            title = dto.title,
                            authorId = dto.author.id,
                            authorName = dto.author.name,
                            authorAvatar = dto.author.avatar,
                            likeCount = dto.favoriteCount.toString(),
                            commentCount = dto.commentCount.toString(),
                            shareCount = "0", // 暂无 shareCount 字段
                            isLiked = dto.isFavorite,
                            isFollowed = dto.author.isFollow
                        )
                    } ?: emptyList()
                    Resource.Success(Pair(models, if (response.hasMore) response.nextCursor else -1L))
                } else {
                    Resource.Error(response.statusMsg ?: "Search failed")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
