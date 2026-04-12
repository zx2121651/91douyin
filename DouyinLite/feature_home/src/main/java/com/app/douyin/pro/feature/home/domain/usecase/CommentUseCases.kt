package com.app.douyin.pro.feature.home.domain.usecase

import com.app.douyin.pro.feature.home.data.CommentRepository
import com.app.douyin.pro.feature.home.domain.model.CommentModel
import com.app.douyin.pro.lib.media.model.Resource
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(videoId: Long): Resource<List<CommentModel>> = repository.getComments(videoId)
}

class PostCommentUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(videoId: Long, content: String, parentId: Long? = null): Resource<CommentModel> = repository.postComment(videoId, content, parentId)
}
