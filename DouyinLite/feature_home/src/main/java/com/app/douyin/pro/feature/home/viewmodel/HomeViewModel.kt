package com.app.douyin.pro.feature.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.lib.media.model.VideoModel
import com.app.douyin.pro.feature.home.domain.model.VideoPage
import com.app.douyin.pro.feature.home.domain.usecase.GetVideosUseCase
import com.app.douyin.pro.feature.home.domain.usecase.LoadMoreVideosUseCase
import com.app.douyin.pro.lib.media.model.AppError
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.interaction.VideoInteractionManager
import com.app.douyin.pro.lib.media.interaction.InteractionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoadState {
    object Idle : LoadState()
    object Loading : LoadState()
    data class Success(val isEmpty: Boolean) : LoadState()
    data class Error(val message: String, val error: AppError? = null) : LoadState()
}

sealed class PagingState {
    object Idle : PagingState()
    object Loading : PagingState()
    data class Error(val message: String) : PagingState()
}

data class HomeUiState(
    val videos: List<VideoModel> = emptyList(),
    val loadState: LoadState = LoadState.Idle,
    val pagingState: PagingState = PagingState.Idle
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase,
    private val loadMoreVideosUseCase: LoadMoreVideosUseCase,
    private val interactionManager: VideoInteractionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var nextTime: Long? = null
    private var lastRequestedTime: Long? = null

    init {
        loadInitialData()
        observeInteractions()
    }

    private fun observeInteractions() {
        viewModelScope.launch {
            interactionManager.interactionEvents.collect { event ->
                val currentVideos = _uiState.value.videos.toMutableList()
                when (event) {
                    is InteractionEvent.LikeChanged -> {
                        val index = currentVideos.indexOfFirst { it.id == event.videoId }
                        if (index != -1) {
                            currentVideos[index] = currentVideos[index].copy(
                                isLiked = event.isLiked,
                                likeCount = event.newLikeCount
                            )
                            _uiState.value = _uiState.value.copy(videos = currentVideos)
                        }
                    }
                    is InteractionEvent.FollowChanged -> {
                        // Update all videos from this author
                        var changed = false
                        currentVideos.forEachIndexed { index, video ->
                            if (video.author.id == event.authorId) {
                                currentVideos[index] = video.copy(author = video.author.copy(isFollowed = event.isFollowed))
                                changed = true
                            }
                        }
                        if (changed) {
                            _uiState.value = _uiState.value.copy(videos = currentVideos)
                        }
                    }
                    is InteractionEvent.CommentAdded -> {
                        val index = currentVideos.indexOfFirst { it.id == event.videoId }
                        if (index != -1) {
                            currentVideos[index] = currentVideos[index].copy(
                                commentCount = event.newCommentCount
                            )
                            _uiState.value = _uiState.value.copy(videos = currentVideos)
                        }
                    }
                }
            }
        }
    }

    fun loadInitialData() {
        if (_uiState.value.loadState is LoadState.Loading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadState = LoadState.Loading)
            when (val result = getVideosUseCase()) {
                is Resource.Success -> {
                    val page = result.data
                    _uiState.value = _uiState.value.copy(
                        videos = page.videos,
                        loadState = LoadState.Success(isEmpty = page.videos.isEmpty())
                    )
                    nextTime = page.nextTime
                    lastRequestedTime = null
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        loadState = LoadState.Error(result.message, result.error)
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(loadState = LoadState.Idle)
                }
            }
        }
    }

    fun loadMore() {
        val currentState = _uiState.value
        if (currentState.pagingState is PagingState.Loading) {
            return
        }

        if (nextTime == null && currentState.videos.isNotEmpty()) {
            return
        }

        if (nextTime != null && nextTime == lastRequestedTime) {
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(pagingState = PagingState.Loading)
            lastRequestedTime = nextTime

            when (val result = loadMoreVideosUseCase(nextTime)) {
                is Resource.Success -> {
                    val page = result.data
                    val newVideos = page.videos
                    val currentVideos = _uiState.value.videos.toMutableList()

                    val existingIds = currentVideos.map { it.id }.toSet()
                    val uniqueNewVideos = newVideos.filter { it.id !in existingIds }

                    currentVideos.addAll(uniqueNewVideos)

                    _uiState.value = _uiState.value.copy(
                        videos = currentVideos,
                        pagingState = PagingState.Idle
                    )
                    nextTime = page.nextTime
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        pagingState = PagingState.Error(result.message)
                    )
                    lastRequestedTime = null
                }
                else -> {
                    _uiState.value = _uiState.value.copy(pagingState = PagingState.Idle)
                }
            }
        }
    }

    fun toggleLike(videoId: Long) {
        val video = _uiState.value.videos.find { it.id == videoId } ?: return
        interactionManager.toggleLike(videoId, video.isLiked, video.likeCount)
    }

    fun toggleFollow(videoId: Long) {
        val video = _uiState.value.videos.find { it.id == videoId } ?: return
        interactionManager.toggleFollow(video.author.id, video.author.isFollowed)
    }
}
