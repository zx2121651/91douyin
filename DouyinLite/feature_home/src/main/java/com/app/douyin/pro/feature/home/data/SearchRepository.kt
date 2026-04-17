package com.app.douyin.pro.feature.home.data

import com.app.douyin.pro.feature.home.data.source.SearchDataSource
import com.app.douyin.pro.lib.media.model.UserModel
import com.app.douyin.pro.lib.media.model.VideoModel
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
                            shareCount = 0L, // 暂无 shareCount 字段
                            isLiked = dto.isFavorite,
                            status = dto.status ?: "published",
                            createdAt = dto.createdAt ?: System.currentTimeMillis()
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

    suspend fun searchUsers(keyword: String, cursor: Long): Resource<Pair<List<UserModel>, Long>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = remoteDataSource.searchUsers(keyword, cursor)
                if (response.statusCode == 0) {
                    val models = response.userList?.map { dto ->
                        UserModel(
                            id = dto.id,
                            name = dto.name,
                            avatar = dto.avatar,
                            followCount = dto.followCount,
                            followerCount = dto.followerCount,
                            isFollowed = dto.isFollow,
                            signature = dto.signature,
                            backgroundImage = dto.backgroundImage
                        )
                    } ?: emptyList()
                    Resource.Success(Pair(models ?: emptyList(), if (response.hasMore) response.nextCursor else -1L))
                } else {
                    Resource.Error(response.statusMsg ?: "Search failed")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown Error")
            }
        }
    }
}
