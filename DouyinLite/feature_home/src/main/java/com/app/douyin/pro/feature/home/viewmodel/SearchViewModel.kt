package com.app.douyin.pro.feature.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.domain.usecase.SearchUsersUseCase
import com.app.douyin.pro.feature.home.domain.usecase.SearchVideosUseCase
import com.app.douyin.pro.lib.media.interaction.InteractionEvent
import com.app.douyin.pro.lib.media.interaction.VideoInteractionManager
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.lib.media.model.UserModel
import com.app.douyin.pro.lib.media.model.VideoModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Searching : SearchUiState()
    object Results : SearchUiState()
    object Empty : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

enum class SearchResultType {
    Video, User
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchVideosUseCase: SearchVideosUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val interactionManager: VideoInteractionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _selectedTab = MutableStateFlow(SearchResultType.Video)
    val selectedTab: StateFlow<SearchResultType> = _selectedTab

    private val _videoResults = MutableStateFlow<List<VideoModel>>(emptyList())
    val videoResults: StateFlow<List<VideoModel>> = _videoResults

    private val _userResults = MutableStateFlow<List<UserModel>>(emptyList())
    val userResults: StateFlow<List<UserModel>> = _userResults

    private val _searchHistory = MutableStateFlow<List<String>>(loadHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var videoCursor: Long = 0L
    private var userCursor: Long = 0L
    private var hasMoreVideos: Boolean = true
    private var hasMoreUsers: Boolean = true
    private var currentKeyword: String = ""

    init {
        observeInteractions()
    }

    private fun observeInteractions() {
        viewModelScope.launch {
            interactionManager.interactionEvents.collect { event ->
                val currentVideos = _videoResults.value.toMutableList()
                when (event) {
                    is InteractionEvent.LikeChanged -> {
                        val index = currentVideos.indexOfFirst { it.id == event.videoId }
                        if (index != -1) {
                            currentVideos[index] = currentVideos[index].copy(
                                isLiked = event.isLiked,
                                likeCount = event.newLikeCount
                            )
                            _videoResults.value = currentVideos
                        }
                    }
                    is InteractionEvent.FollowChanged -> {
                        var changed = false
                        currentVideos.forEachIndexed { index, video ->
                            if (video.author.id == event.authorId) {
                                currentVideos[index] = video.copy(author = video.author.copy(isFollowed = event.isFollowed))
                                changed = true
                            }
                        }
                        if (changed) {
                            _videoResults.value = currentVideos
                        }

                        val currentUsers = _userResults.value.toMutableList()
                        currentUsers.forEachIndexed { index, user ->
                            if (user.id == event.authorId) {
                                currentUsers[index] = user.copy(isFollowed = event.isFollowed)
                                changed = true
                            }
                        }
                        if (changed) {
                            _userResults.value = currentUsers
                        }
                    }
                    is InteractionEvent.CommentAdded -> {
                        val index = currentVideos.indexOfFirst { it.id == event.videoId }
                        if (index != -1) {
                            currentVideos[index] = currentVideos[index].copy(
                                commentCount = event.newCommentCount
                            )
                            _videoResults.value = currentVideos
                        }
                    }
                }
            }
        }
    }

    fun onQueryChanged(query: String) {
        if (query.isEmpty()) {
            _uiState.value = SearchUiState.Idle
        } else if (_uiState.value is SearchUiState.Idle) {
            _uiState.value = SearchUiState.Searching
        }
    }

    fun search(keyword: String) {
        if (keyword.isBlank()) return

        currentKeyword = keyword
        saveHistory(keyword)

        videoCursor = 0L
        userCursor = 0L
        hasMoreVideos = true
        hasMoreUsers = true
        _videoResults.value = emptyList()
        _userResults.value = emptyList()

        _uiState.value = SearchUiState.Results
        performSearch()
    }

    fun selectTab(type: SearchResultType) {
        if (_selectedTab.value == type) return
        _selectedTab.value = type
        if (type == SearchResultType.Video && _videoResults.value.isEmpty()) {
            performSearch()
        } else if (type == SearchResultType.User && _userResults.value.isEmpty()) {
            performSearch()
        }
    }

    private fun performSearch() {
        if (currentKeyword.isBlank()) return
        if (_selectedTab.value == SearchResultType.Video) {
            loadMoreVideos()
        } else {
            loadMoreUsers()
        }
    }

    fun loadMore() {
        performSearch()
    }

    private fun loadMoreVideos() {
        if (!hasMoreVideos || _isLoading.value) return

        _isLoading.value = true
        viewModelScope.launch {
            when (val result = searchVideosUseCase(currentKeyword, videoCursor)) {
                is Resource.Success -> {
                    val (newVideos, nextCursor) = result.data
                    _videoResults.value = _videoResults.value + newVideos
                    if (nextCursor == -1L) {
                        hasMoreVideos = false
                    } else {
                        videoCursor = nextCursor
                    }
                    if (_videoResults.value.isEmpty()) {
                        _uiState.value = SearchUiState.Empty
                    }
                }
                is Resource.Error -> {
                    if (_videoResults.value.isEmpty()) {
                        _uiState.value = SearchUiState.Error(result.message ?: "Search failed")
                    }
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    private fun loadMoreUsers() {
        if (!hasMoreUsers || _isLoading.value) return

        _isLoading.value = true
        viewModelScope.launch {
            when (val result = searchUsersUseCase(currentKeyword, userCursor)) {
                is Resource.Success -> {
                    val (newUsers, nextCursor) = result.data
                    _userResults.value = _userResults.value + newUsers
                    if (nextCursor == -1L) {
                        hasMoreUsers = false
                    } else {
                        userCursor = nextCursor
                    }
                    if (_userResults.value.isEmpty() && _videoResults.value.isEmpty()) {
                         // Only set Empty if both are empty?
                         // No, if the current tab is empty, we might want to show empty.
                         // But usually we stay in Results state and show an empty list.
                    }
                }
                is Resource.Error -> {
                    // Handle error
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    private fun loadHistory(): List<String> {
        val historyString = prefs.getString("history_list", "") ?: ""
        return if (historyString.isEmpty()) emptyList() else historyString.split("|")
    }

    private fun saveHistory(keyword: String) {
        if (keyword.isBlank()) return
        val currentHistory = loadHistory().toMutableList()
        currentHistory.remove(keyword)
        currentHistory.add(0, keyword)
        val limitedHistory = currentHistory.take(20)
        prefs.edit().putString("history_list", limitedHistory.joinToString("|")).apply()
        _searchHistory.value = limitedHistory
    }

    fun deleteHistory(keyword: String) {
        val currentHistory = loadHistory().toMutableList()
        currentHistory.remove(keyword)
        prefs.edit().putString("history_list", currentHistory.joinToString("|")).apply()
        _searchHistory.value = currentHistory
    }

    fun clearHistory() {
        prefs.edit().remove("history_list").apply()
        _searchHistory.value = emptyList()
    }

    fun toggleLike(videoId: Long) {
        val video = _videoResults.value.find { it.id == videoId } ?: return
        interactionManager.toggleLike(videoId, video.isLiked, video.likeCount)
    }

    fun toggleFollow(userId: Long) {
        val isFollowed = _userResults.value.find { it.id == userId }?.isFollowed
            ?: _videoResults.value.find { it.author.id == userId }?.author?.isFollowed
            ?: false
        interactionManager.toggleFollow(userId, isFollowed)
    }
}
