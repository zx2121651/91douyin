package com.app.douyin.pro.feature.home.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.feature.home.domain.usecase.GetVideosUseCase
import com.app.douyin.pro.feature.home.domain.usecase.LoadMoreVideosUseCase
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.network.DouyinApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase,
    private val loadMoreVideosUseCase: LoadMoreVideosUseCase,
    private val apiService: DouyinApiService
) : ViewModel() {

    private val _videos = mutableStateListOf<VideoModel>()
    val videos: List<VideoModel> = _videos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var pageCount = 1

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = getVideosUseCase()) {
                is Resource.Success -> _videos.addAll(result.data)
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadMore() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = loadMoreVideosUseCase(pageCount)) {
                is Resource.Success -> {
                    _videos.addAll(result.data)
                    pageCount++
                }
                is Resource.Error -> _error.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun toggleLike(videoId: Long) {
        val index = _videos.indexOfFirst { it.id == videoId }
        if (index != -1) {
            val video = _videos[index]
            val newIsLiked = !video.isLiked

            // Optimistic update
            _videos[index] = video.copy(
                isLiked = newIsLiked,
                likeCount = adjustCount(video.likeCount, if (newIsLiked) 1 else -1)
            )

            // Send to backend
            viewModelScope.launch {
                try {
                    val actionType = if (newIsLiked) 1 else 2
                    val response = apiService.favoriteAction(videoId, actionType)
                    if (response.statusCode != 0) {
                        // Revert on failure
                        _videos[index] = video
                    }
                } catch (e: Exception) {
                    // Revert on failure
                    _videos[index] = video
                }
            }
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
        val index = _videos.indexOfFirst { it.id == videoId }
        if (index != -1) {
            val video = _videos[index]
            val newIsFollowed = !video.isFollowed

            _videos[index] = video.copy(isFollowed = newIsFollowed)

            viewModelScope.launch {
                try {
                    val actionType = if (newIsFollowed) 1 else 2
                    val response = apiService.relationAction(video.authorId, actionType)
                    if (response.statusCode != 0) {
                        _videos[index] = video // revert on failure
                    }
                } catch (e: Exception) {
                    _videos[index] = video // revert on failure
                }
            }
        }
    }
}
