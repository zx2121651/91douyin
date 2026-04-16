package com.app.douyin.pro.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.domain.model.CommentModel
import com.app.douyin.pro.feature.home.domain.model.CommentStatus
import com.app.douyin.pro.feature.home.domain.usecase.GetCommentsUseCase
import com.app.douyin.pro.feature.home.domain.usecase.PostCommentUseCase
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class CommentUiState {
    object Loading : CommentUiState()
    data class Success(val comments: List<CommentModel>) : CommentUiState()
    data class Error(val message: String) : CommentUiState()
}

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val postCommentUseCase: PostCommentUseCase,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommentUiState>(CommentUiState.Loading)
    val uiState: StateFlow<CommentUiState> = _uiState.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentModel>>(emptyList())
    val comments: StateFlow<List<CommentModel>> = _comments.asStateFlow()

    private var currentVideoId: Long = -1L

    fun loadComments(videoId: Long) {
        currentVideoId = videoId
        viewModelScope.launch {
            _uiState.value = CommentUiState.Loading
            when (val result = getCommentsUseCase(videoId)) {
                is Resource.Success -> {
                    _comments.value = result.data
                    _uiState.value = CommentUiState.Success(result.data)
                }
                is Resource.Error -> {
                    _uiState.value = CommentUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun postComment(videoId: Long, content: String, parentId: Long? = null, onComplete: () -> Unit = {}) {
        val tempId = UUID.randomUUID().toString()
        val authorName = "我" // In a real app, get from authManager or user profile
        val newComment = CommentModel(
            id = -1,
            authorName = authorName,
            authorAvatar = null,
            content = content,
            createDate = "刚刚",
            replyCount = 0,
            status = CommentStatus.SENDING,
            tempId = tempId
        )

        // Optimistic update
        _comments.value = listOf(newComment) + _comments.value
        if (_uiState.value is CommentUiState.Success) {
            _uiState.value = CommentUiState.Success(_comments.value)
        }

        executePostComment(videoId, content, parentId, tempId, onComplete)
    }

    fun retryPostComment(comment: CommentModel, videoId: Long) {
        // Update status to SENDING
        _comments.value = _comments.value.map {
            if (it.tempId == comment.tempId) it.copy(status = CommentStatus.SENDING) else it
        }
        if (_uiState.value is CommentUiState.Success) {
            _uiState.value = CommentUiState.Success(_comments.value)
        }

        executePostComment(videoId, comment.content, null, comment.tempId ?: "")
    }

    fun retryLoadComments() {
        if (currentVideoId != -1L) {
            loadComments(currentVideoId)
        }
    }

    private fun executePostComment(videoId: Long, content: String, parentId: Long?, tempId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            when (val result = postCommentUseCase(videoId, content, parentId)) {
                is Resource.Success -> {
                    _comments.value = _comments.value.map {
                        if (it.tempId == tempId) result.data.copy(status = CommentStatus.SUCCESS, tempId = tempId) else it
                    }
                    onComplete()
                }
                is Resource.Error -> {
                    _comments.value = _comments.value.map {
                        if (it.tempId == tempId) it.copy(status = CommentStatus.FAILED) else it
                    }
                }
                else -> {}
            }
            if (_uiState.value is CommentUiState.Success) {
                _uiState.value = CommentUiState.Success(_comments.value)
            }
        }
    }
}
