package com.app.douyin.pro.feature.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.home.domain.usecase.SearchUsersUseCase
import com.app.douyin.pro.feature.home.domain.usecase.SearchVideosUseCase
import com.app.douyin.pro.lib.media.interaction.InteractionEvent
import com.app.douyin.pro.lib.media.interaction.VideoInteractionManager
import com.app.douyin.pro.lib.media.model.Resource
import com.app.douyin.pro.feature.home.data.SearchRepository
import com.app.douyin.pro.lib.media.model.UserModel
import com.app.douyin.pro.lib.media.model.VideoModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Suggesting : SearchUiState()
    object Results : SearchUiState()
    object Empty : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

enum class SuggestionType {
    HISTORY, HOT, SUGGEST
}

data class SearchSuggestion(
    val content: String,
    val type: SuggestionType
)

enum class SearchResultType {
    Video, User
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchVideosUseCase: SearchVideosUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val searchRepository: SearchRepository,
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

    private val _pagingState = MutableStateFlow<PagingState>(PagingState.Idle)
    val pagingState: StateFlow<PagingState> = _pagingState

    private val _searchHistory = MutableStateFlow<List<String>>(loadHistory())
    private val _hotWords = MutableStateFlow<List<String>>(emptyList())
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())

    private val _combinedSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val combinedSuggestions: StateFlow<List<SearchSuggestion>> = _combinedSuggestions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var videoCursor: Long = 0L
    private var userCursor: Long = 0L
    private var hasMoreVideos: Boolean = true
    private var hasMoreUsers: Boolean = true
    private var currentKeyword: String = ""

    private val _queryFlow = MutableStateFlow("")

    init {
        observeInteractions()
        observeQuery()
        loadHotWords()
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeQuery() {
        viewModelScope.launch {
            _queryFlow
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    flow {
                        if (query.isBlank()) {
                            emit(emptyList<String>())
                        } else {
                            val result = searchRepository.getSuggestions(query)
                            if (result is Resource.Success) {
                                emit(result.data)
                            } else {
                                emit(emptyList<String>())
                            }
                        }
                    }
                }
                .collect { suggestions ->
                    _suggestions.value = suggestions
                }
        }

        // Also observe history and hot words to update combined suggestions
        viewModelScope.launch {
            combine(_searchHistory, _hotWords, _suggestions, _queryFlow) { history, hot, suggest, query ->
                buildCombinedSuggestions(history, hot, suggest, query)
            }.collect {
                _combinedSuggestions.value = it
            }
        }
    }

    private fun buildCombinedSuggestions(
        history: List<String>,
        hot: List<String>,
        suggest: List<String>,
        query: String
    ): List<SearchSuggestion> {
        return if (query.isEmpty()) {
            val result = mutableListOf<SearchSuggestion>()
            history.forEach { result.add(SearchSuggestion(it, SuggestionType.HISTORY)) }
            hot.forEach { result.add(SearchSuggestion(it, SuggestionType.HOT)) }
            result
        } else {
            suggest.map { SearchSuggestion(it, SuggestionType.SUGGEST) }
        }
    }


    private fun loadHotWords() {
        viewModelScope.launch {
            when (val result = searchRepository.getHotWords()) {
                is Resource.Success -> {
                    _hotWords.value = result.data
                }
                else -> {}
            }
        }
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
        val oldQuery = _queryFlow.value
        _queryFlow.value = query

        // If we were in Results state and user starts editing, move back to Suggesting
        if (query != oldQuery && _uiState.value is SearchUiState.Results) {
            _uiState.value = SearchUiState.Suggesting
        }

        if (query.isEmpty()) {
            _uiState.value = SearchUiState.Idle
            _suggestions.value = emptyList()
        } else if (_uiState.value is SearchUiState.Idle) {
            _uiState.value = SearchUiState.Suggesting
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
        if (!hasMoreVideos || _isLoading.value || _pagingState.value is PagingState.Loading) return

        val isInitial = _videoResults.value.isEmpty()
        if (isInitial) {
            _isLoading.value = true
        } else {
            _pagingState.value = PagingState.Loading
        }

        viewModelScope.launch {
            when (val result = searchVideosUseCase(currentKeyword, videoCursor)) {
                is Resource.Success -> {
                    val (newVideos, nextCursor) = result.data
                    val currentVideos = _videoResults.value.toMutableList()
                    val existingIds = currentVideos.map { it.id }.toSet()
                    val uniqueNewVideos = newVideos.filter { it.id !in existingIds }

                    _videoResults.value = currentVideos + uniqueNewVideos

                    if (nextCursor == -1L || nextCursor == videoCursor) {
                        hasMoreVideos = false
                    } else {
                        videoCursor = nextCursor
                        hasMoreVideos = true
                    }

                    if (_videoResults.value.isEmpty()) {
                        _uiState.value = SearchUiState.Empty
                    } else {
                        _uiState.value = SearchUiState.Results
                    }
                    _pagingState.value = PagingState.Idle
                }
                is Resource.Error -> {
                    if (isInitial) {
                        _uiState.value = SearchUiState.Error(result.message)
                    } else {
                        _pagingState.value = PagingState.Error(result.message)
                    }
                }
                else -> {
                    _pagingState.value = PagingState.Idle
                }
            }
            _isLoading.value = false
        }
    }

    private fun loadMoreUsers() {
        if (!hasMoreUsers || _isLoading.value || _pagingState.value is PagingState.Loading) return

        val isInitial = _userResults.value.isEmpty()
        if (isInitial) {
            _isLoading.value = true
        } else {
            _pagingState.value = PagingState.Loading
        }

        viewModelScope.launch {
            when (val result = searchUsersUseCase(currentKeyword, userCursor)) {
                is Resource.Success -> {
                    val (newUsers, nextCursor) = result.data
                    val currentUsers = _userResults.value.toMutableList()
                    val existingIds = currentUsers.map { it.id }.toSet()
                    val uniqueNewUsers = newUsers.filter { it.id !in existingIds }

                    _userResults.value = currentUsers + uniqueNewUsers

                    if (nextCursor == -1L || nextCursor == userCursor) {
                        hasMoreUsers = false
                    } else {
                        userCursor = nextCursor
                        hasMoreUsers = true
                    }
                    _pagingState.value = PagingState.Idle
                }
                is Resource.Error -> {
                    if (!isInitial) {
                        _pagingState.value = PagingState.Error(result.message)
                    }
                }
                else -> {
                    _pagingState.value = PagingState.Idle
                }
            }
            _isLoading.value = false
        }
    }

    private fun loadHistory(): List<String> {
        val historyString = prefs.getString("history_list", "")
        return if (historyString.isNullOrEmpty()) {
            emptyList()
        } else {
            historyString.split("|").take(20)
        }
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
