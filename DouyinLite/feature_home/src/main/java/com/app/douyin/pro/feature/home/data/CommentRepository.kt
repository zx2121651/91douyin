package com.app.douyin.pro.feature.home.data

import com.app.douyin.pro.feature.home.data.source.CommentDataSource
import com.app.douyin.pro.feature.home.domain.model.CommentModel
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.AppError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val dataSource: CommentDataSource
) {
    suspend fun getComments(videoId: Long): Resource<List<CommentModel>> = try {
        Resource.Success(dataSource.getComments(videoId))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown Error", AppError.NetworkError)
    }

    suspend fun postComment(videoId: Long, content: String, parentId: Long?): Resource<CommentModel> = try {
        Resource.Success(dataSource.postComment(videoId, content, parentId))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Publish Failed", AppError.NetworkError)
    }
}
