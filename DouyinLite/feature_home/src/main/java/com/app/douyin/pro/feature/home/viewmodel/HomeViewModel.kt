package com.app.douyin.pro.feature.home.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.domain.usecase.GetVideosUseCase
import com.app.douyin.pro.feature.home.domain.usecase.LoadMoreVideosUseCase
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase,
    private val loadMoreVideosUseCase: LoadMoreVideosUseCase
) : ViewModel() {

    private val _videos = mutableStateListOf<String>()
    val videos: List<String> = _videos

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
}
