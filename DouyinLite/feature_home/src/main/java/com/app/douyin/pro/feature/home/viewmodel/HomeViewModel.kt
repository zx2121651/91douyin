package com.app.douyin.pro.feature.home.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.feature.home.domain.usecase.GetVideosUseCase
import com.app.douyin.pro.feature.home.domain.usecase.LoadMoreVideosUseCase
import com.app.douyin.pro.lib.media.model.AppError
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.network.DouyinApiService
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
    private val apiService: DouyinApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var pageCount = 1

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        if (_uiState.value.loadState is LoadState.Loading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loadState = LoadState.Loading)
            when (val result = getVideosUseCase()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        videos = result.data,
                        loadState = LoadState.Success(isEmpty = result.data.isEmpty())
                    )
                    pageCount = 1
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
        if (_uiState.value.pagingState is PagingState.Loading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(pagingState = PagingState.Loading)
            when (val result = loadMoreVideosUseCase(pageCount)) {
                is Resource.Success -> {
                    val currentVideos = _uiState.value.videos.toMutableList()
                    currentVideos.addAll(result.data)
                    _uiState.value = _uiState.value.copy(
                        videos = currentVideos,
                        pagingState = PagingState.Idle
                    )
                    pageCount++
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        pagingState = PagingState.Error(result.message)
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(pagingState = PagingState.Idle)
                }
            }
        }
    }

    fun toggleLike(videoId: Long) {
        val currentVideos = _uiState.value.videos.toMutableList()
        val index = currentVideos.indexOfFirst { it.id == videoId }
        if (index != -1) {
            val video = currentVideos[index]
            val newIsLiked = !video.isLiked

            // Optimistic update
            currentVideos[index] = video.copy(
                isLiked = newIsLiked,
                likeCount = adjustCount(video.likeCount, if (newIsLiked) 1 else -1)
            )
            _uiState.value = _uiState.value.copy(videos = currentVideos)

            // Send to backend
            viewModelScope.launch {
                try {
                    val actionType = if (newIsLiked) 1 else 2
                    val response = apiService.favoriteAction(videoId, actionType)
                    if (response.statusCode != 0) {
                        revertLike(videoId, video)
                    }
                } catch (e: Exception) {
                    revertLike(videoId, video)
                }
            }
        }
    }

    private fun revertLike(videoId: Long, originalVideo: VideoModel) {
        val currentVideos = _uiState.value.videos.toMutableList()
        val index = currentVideos.indexOfFirst { it.id == videoId }
        if (index != -1) {
            currentVideos[index] = originalVideo
            _uiState.value = _uiState.value.copy(videos = currentVideos)
        }
    }

    private fun adjustCount(countStr: String, delta: Int): String {
        if (countStr.contains("w")) return countStr // Too lazy to parse "1.2w", keep it as is
        return try {
            val num = countStr.toInt()
            (num + delta).toString()
        } catch (e: Exception) {
            countStr
        }
    }

    fun toggleFollow(videoId: Long) {
        val currentVideos = _uiState.value.videos.toMutableList()
        val index = currentVideos.indexOfFirst { it.id == videoId }
        if (index != -1) {
            val video = currentVideos[index]
            val newIsFollowed = !video.isFollowed

            currentVideos[index] = video.copy(isFollowed = newIsFollowed)
            _uiState.value = _uiState.value.copy(videos = currentVideos)

            viewModelScope.launch {
                try {
                    val actionType = if (newIsFollowed) 1 else 2
                    val response = apiService.relationAction(video.authorId, actionType)
                    if (response.statusCode != 0) {
                        revertFollow(videoId, video)
                    }
                } catch (e: Exception) {
                    revertFollow(videoId, video)
                }
            }
        }
    }

    private fun revertFollow(videoId: Long, originalVideo: VideoModel) {
        val currentVideos = _uiState.value.videos.toMutableList()
        val index = currentVideos.indexOfFirst { it.id == videoId }
        if (index != -1) {
            currentVideos[index] = originalVideo
            _uiState.value = _uiState.value.copy(videos = currentVideos)
        }
    }
}
