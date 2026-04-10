package com.app.douyin.pro.feature.home.data.source

import com.app.douyin.pro.feature.home.domain.model.CommentModel
import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.network.model.CommentDto
import com.app.douyin.pro.lib.media.auth.AuthManager
import javax.inject.Inject

interface CommentDataSource {
    suspend fun getComments(videoId: Long): List<CommentModel>
    suspend fun postComment(videoId: Long, content: String, parentId: Long?): CommentModel
}

class RemoteCommentDataSource @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) : CommentDataSource {

    override suspend fun getComments(videoId: Long): List<CommentModel> {
        val token = authManager.getToken()
        try {
            val response = apiService.getCommentList(videoId, token)
            if (response.statusCode == 0) {
                response.commentList?.let { list ->
                    return list.map { mapDtoToModel(it) }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return emptyList()
    }

    override suspend fun postComment(videoId: Long, content: String, parentId: Long?): CommentModel {
        val token = authManager.getToken() ?: throw Exception("Not logged in")
        val response = apiService.postComment(videoId, 1, content, parentId, token)

        if (response.statusCode == 0) {
            response.comment?.let {
                return mapDtoToModel(it)
            }
        }
        throw Exception(response.statusMsg ?: "Post failed")
    }

    private fun mapDtoToModel(dto: CommentDto): CommentModel {
        val mappedReplies = dto.replies?.map { mapDtoToModel(it) } ?: emptyList()
        return CommentModel(
            id = dto.id,
            authorName = dto.user.name,
            authorAvatar = dto.user.avatar,
            content = dto.content,
            createDate = dto.createDate,
            replyCount = dto.replyCount,
            replies = mappedReplies
        )
    }
}
