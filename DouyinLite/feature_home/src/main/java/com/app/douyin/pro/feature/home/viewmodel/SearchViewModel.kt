package com.app.douyin.pro.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.domain.model.VideoModel
import com.app.douyin.pro.feature.home.domain.usecase.SearchVideosUseCase
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchVideosUseCase: SearchVideosUseCase
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
}
