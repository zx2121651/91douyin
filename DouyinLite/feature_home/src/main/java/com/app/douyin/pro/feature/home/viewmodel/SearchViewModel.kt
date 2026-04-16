package com.app.douyin.pro.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.lib.media.model.VideoModel
import com.app.douyin.pro.feature.home.domain.usecase.SearchVideosUseCase
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.interaction.VideoInteractionManager
import com.app.douyin.pro.lib.media.interaction.InteractionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchVideosUseCase: SearchVideosUseCase,
    private val interactionManager: VideoInteractionManager
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<VideoModel>>(emptyList())
    val searchResults: StateFlow<List<VideoModel>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var nextCursor: Long = 0L
    private var hasMore: Boolean = true
    private var currentKeyword: String = ""

    init {
        observeInteractions()
    }

    private fun observeInteractions() {
        viewModelScope.launch {
            interactionManager.interactionEvents.collect { event ->
                val currentResults = _searchResults.value.toMutableList()
                when (event) {
                    is InteractionEvent.LikeChanged -> {
                        val index = currentResults.indexOfFirst { it.id == event.videoId }
                        if (index != -1) {
                            currentResults[index] = currentResults[index].copy(
                                isLiked = event.isLiked,
                                likeCount = event.newLikeCount
                            )
                            _searchResults.value = currentResults
                        }
                    }
                    is InteractionEvent.FollowChanged -> {
                        var changed = false
                        currentResults.forEachIndexed { index, video ->
                            if (video.author.id == event.authorId) {
                                currentResults[index] = video.copy(author = video.author.copy(isFollowed = event.isFollowed))
                                changed = true
                            }
                        }
                        if (changed) {
                            _searchResults.value = currentResults
                        }
                    }
                    is InteractionEvent.CommentAdded -> {
                        val index = currentResults.indexOfFirst { it.id == event.videoId }
                        if (index != -1) {
                            currentResults[index] = currentResults[index].copy(
                                commentCount = event.newCommentCount
                            )
                            _searchResults.value = currentResults
                        }
                    }
                }
            }
        }
    }

    fun search(keyword: String) {
        if (keyword.isBlank()) return

        currentKeyword = keyword
        nextCursor = 0L
        hasMore = true
        _searchResults.value = emptyList()

        loadMore()
    }

    fun loadMore() {
        if (!hasMore || _isLoading.value || currentKeyword.isBlank()) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            when (val result = searchVideosUseCase(currentKeyword, nextCursor)) {
                is Resource.Success -> {
                    val (newVideos, newCursor) = result.data
                    _searchResults.value = _searchResults.value + newVideos

                    if (newCursor == -1L) {
                        hasMore = false
                    } else {
                        nextCursor = newCursor
                    }
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                is Resource.Loading -> {
                    // Handled by _isLoading
                }
            }
            _isLoading.value = false
        }
    }

    fun toggleLike(videoId: Long) {
        val video = _searchResults.value.find { it.id == videoId } ?: return
        interactionManager.toggleLike(videoId, video.isLiked, video.likeCount)
    }

    fun toggleFollow(videoId: Long) {
        val video = _searchResults.value.find { it.id == videoId } ?: return
        interactionManager.toggleFollow(video.author.id, video.author.isFollowed)
    }
}
