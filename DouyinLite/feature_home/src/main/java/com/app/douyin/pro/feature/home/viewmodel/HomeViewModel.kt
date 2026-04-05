package com.app.douyin.pro.feature.home.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.data.HomeRepository
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _videos = mutableStateListOf<String>()
    val videos: List<String> = _videos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var pageCount = 1

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            when (val result = repository.getInitialVideos()) {
                is Resource.Success -> _videos.addAll(result.data)
                else -> {}
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.loadMoreVideos(pageCount)) {
                is Resource.Success -> {
                    _videos.addAll(result.data)
                    pageCount++
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }
}
