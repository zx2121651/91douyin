package com.app.douyin.pro.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.domain.model.CommentModel
import com.app.douyin.pro.feature.home.domain.usecase.GetCommentsUseCase
import com.app.douyin.pro.feature.home.domain.usecase.PostCommentUseCase
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val postCommentUseCase: PostCommentUseCase
) : ViewModel() {

    private val _comments = MutableStateFlow<List<CommentModel>>(emptyList())
    val comments: StateFlow<List<CommentModel>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadComments(videoId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getCommentsUseCase(videoId)) {
                is Resource.Success -> _comments.value = result.data
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun postComment(videoId: Long, content: String, parentId: Long? = null, onComplete: () -> Unit) {
        viewModelScope.launch {
            when (val result = postCommentUseCase(videoId, content, parentId)) {
                is Resource.Success -> {
                    // Optimistic UI update could be done here. For simplicity, just reload the list
                    loadComments(videoId)
                    onComplete()
                }
                else -> {}
            }
        }
    }
}
