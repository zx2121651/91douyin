package com.app.douyin.pro.feature.home.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.feature.home.domain.model.VideoPage
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

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var nextTime: Long? = null
    private var lastRequestedTime: Long? = null

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        if (_uiState.value.loadState is LoadState.Loading) return

        viewModelScope.launch {
            // Log.d(TAG, "loadInitialData: starting")
            _uiState.value = _uiState.value.copy(loadState = LoadState.Loading)
            when (val result = getVideosUseCase()) {
                is Resource.Success -> {
                    val page = result.data
                    // Log.d(TAG, "loadInitialData success: videos=${page.videos.size}, nextTime=${page.nextTime}")
                    _uiState.value = _uiState.value.copy(
                        videos = page.videos,
                        loadState = LoadState.Success(isEmpty = page.videos.isEmpty())
                    )
                    nextTime = page.nextTime
                    lastRequestedTime = null
                }
                is Resource.Error -> {
                    // Log.e(TAG, "loadInitialData error: ${result.message}")
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
            // Log.d(TAG, "loadMore: already loading, ignore")
            return
        }

        // Boundary protection: if nextTime is null and we already have data, it means no more data
        if (nextTime == null && currentState.videos.isNotEmpty()) {
            // Log.d(TAG, "loadMore: nextTime is null, no more data")
            return
        }

        // Prevent redundant requests for the same timestamp
        if (nextTime != null && nextTime == lastRequestedTime) {
            // Log.w(TAG, "loadMore: nextTime $nextTime is same as lastRequestedTime, potential loop, ignore")
            return
        }

        viewModelScope.launch {
            // Log.d(TAG, "loadMore: starting with nextTime=$nextTime")
            _uiState.value = currentState.copy(pagingState = PagingState.Loading)
            lastRequestedTime = nextTime

            when (val result = loadMoreVideosUseCase(nextTime)) {
                is Resource.Success -> {
                    val page = result.data
                    val newVideos = page.videos
                    val currentVideos = _uiState.value.videos.toMutableList()

                    // Deduplication logic
                    val existingIds = currentVideos.map { it.id }.toSet()
                    val uniqueNewVideos = newVideos.filter { it.id !in existingIds }

                    // if (uniqueNewVideos.size < newVideos.size) {
                    //     Log.d(TAG, "loadMore: deduplicated ${newVideos.size - uniqueNewVideos.size} videos")
                    // }

                    currentVideos.addAll(uniqueNewVideos)

                    // Log.d(TAG, "loadMore success: added ${uniqueNewVideos.size} videos, total=${currentVideos.size}, nextTime=${page.nextTime}")

                    _uiState.value = _uiState.value.copy(
                        videos = currentVideos,
                        pagingState = PagingState.Idle
                    )
                    nextTime = page.nextTime
                }
                is Resource.Error -> {
                    // Log.e(TAG, "loadMore error: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        pagingState = PagingState.Error(result.message)
                    )
                    // Reset lastRequestedTime on error so it can be retried
                    lastRequestedTime = null
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
