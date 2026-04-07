package com.app.douyin.pro.feature.home.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.domain.usecase.GetVideosUseCase
import com.app.douyin.pro.feature.home.domain.usecase.LoadMoreVideosUseCase
import com.app.douyin.pro.feature.home.domain.usecase.FavoriteActionUseCase
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.network.VideoDto
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
    private val favoriteActionUseCase: FavoriteActionUseCase
) : ViewModel() {

    private val _videos = mutableStateListOf<VideoDto>()
    val videos: List<VideoDto> = _videos

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
                is Resource.Success -> {
                    _videos.clear()
                    _videos.addAll(result.data)
                }
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

    // 乐观更新点赞状态 (Optimistic Update for Like status)
    fun toggleFavorite(videoId: Long) {
        val index = _videos.indexOfFirst { it.id == videoId }
        if (index != -1) {
            val video = _videos[index]
            val newIsFavorite = !video.is_favorite
            val newFavoriteCount = if (newIsFavorite) video.favorite_count + 1 else video.favorite_count - 1

            // 乐观更新 UI
            _videos[index] = video.copy(is_favorite = newIsFavorite, favorite_count = newFavoriteCount)

            // 发起网络请求
            viewModelScope.launch {
                val result = favoriteActionUseCase(videoId, newIsFavorite)
                if (result !is Resource.Success) {
                    // 如果失败，回滚状态
                    _videos[index] = video // revert back
                    _error.value = "点赞操作失败" // "Favorite action failed"
                }
            }
        }
    }
}
