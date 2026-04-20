package com.app.douyin.pro.feature.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.profile.data.source.ProfileInfo
import com.app.douyin.pro.feature.profile.domain.usecase.GetFavoriteVideosUseCase
import com.app.douyin.pro.feature.profile.domain.usecase.GetProfileInfoUseCase
import com.app.douyin.pro.lib.media.model.Resource

import com.app.douyin.pro.lib.media.model.PagingState
import com.app.douyin.pro.feature.profile.domain.usecase.GetPublishedVideosUseCase
import com.app.douyin.pro.lib.media.auth.AuthManager
import com.app.douyin.pro.lib.media.auth.AuthRepository
import com.app.douyin.pro.lib.media.auth.SessionState
import com.app.douyin.pro.lib.media.interaction.InteractionEvent
import com.app.douyin.pro.lib.media.interaction.VideoInteractionManager
import com.app.douyin.pro.lib.media.model.VideoModel
import com.app.douyin.pro.lib.media.util.CountFormatter

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProfileViewMode {
    SELF, VISITOR
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileInfoUseCase: GetProfileInfoUseCase,
    private val getPublishedVideosUseCase: GetPublishedVideosUseCase,
    private val getFavoriteVideosUseCase: GetFavoriteVideosUseCase,
    private val authRepository: AuthRepository,
    private val authManager: AuthManager,
    private val interactionManager: VideoInteractionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val userId: Long? = savedStateHandle.get<Long>("userId")?.takeIf { it > 0 }

    private val _viewMode = MutableStateFlow(if (isSelf()) ProfileViewMode.SELF else ProfileViewMode.VISITOR)
    val viewMode: StateFlow<ProfileViewMode> = _viewMode.asStateFlow()

    val sessionState: StateFlow<SessionState> = authRepository.getSessionState()

    private val _profileInfo = MutableStateFlow<ProfileInfo?>(null)
    val profileInfo: StateFlow<ProfileInfo?> = _profileInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _pagingState = MutableStateFlow<PagingState>(PagingState.Idle)
    val pagingState: StateFlow<PagingState> = _pagingState.asStateFlow()

    private val _publishedVideos = MutableStateFlow<List<VideoModel>>(emptyList())
    val publishedVideos: StateFlow<List<VideoModel>> = _publishedVideos.asStateFlow()

    private val _likedVideos = MutableStateFlow<List<VideoModel>>(emptyList())
    val likedVideos: StateFlow<List<VideoModel>> = _likedVideos.asStateFlow()

    private var videoCursor: Long = 0L
    private var hasMore: Boolean = true

    private var likedVideoCursor: Long = 0L
    private var likedHasMore: Boolean = true

    init {
        viewModelScope.launch {
            sessionState.collect { state ->
                if (state is SessionState.LoggedIn) {
                    refresh()
                } else if (userId != null) {
                    refresh()
                } else {
                    _profileInfo.value = null
                    _publishedVideos.value = emptyList()
                }
            }
        }
        observeInteractions()
    }

    private fun observeInteractions() {
        viewModelScope.launch {
            interactionManager.interactionEvents.collect { event ->
                when (event) {
                    is InteractionEvent.FollowChanged -> {
                        val currentInfo = _profileInfo.value
                        if (currentInfo != null && currentInfo.id == event.authorId) {
                            val newFollowerCount = if (event.isFollowed) {
                                currentInfo.followerCount + 1
                            } else {
                                (currentInfo.followerCount - 1).coerceAtLeast(0)
                            }
                            _profileInfo.value = currentInfo.copy(
                                isFollowed = event.isFollowed,
                                followerCount = newFollowerCount,
                                followers = CountFormatter.format(newFollowerCount)
                            )
                        }
                    }
                    is InteractionEvent.VideoPublished -> {
                        if (isSelf()) {
                            refresh()
                        }
                    }
                    is InteractionEvent.ProfileUpdated -> {
                        if (isSelf()) {
                            refresh()
                        }
                    }
                    is InteractionEvent.LikeChanged -> {
                        // Update published videos
                        val currentVideos = _publishedVideos.value.toMutableList()
                        val index = currentVideos.indexOfFirst { it.id == event.videoId }
                        if (index != -1) {
                            currentVideos[index] = currentVideos[index].copy(
                                isLiked = event.isLiked,
                                likeCount = event.newLikeCount
                            )
                            _publishedVideos.value = currentVideos
                        }

                        // Update liked videos
                        val currentLiked = _likedVideos.value.toMutableList()
                        val likedIndex = currentLiked.indexOfFirst { it.id == event.videoId }
                        if (isSelf()) {
                            if (!event.isLiked) {
                                // If I unliked a video, remove it from my liked list
                                if (likedIndex != -1) {
                                    currentLiked.removeAt(likedIndex)
                                    _likedVideos.value = currentLiked
                                }
                            } else {
                                // If I liked a video, we might want to refresh to get it at the top
                                // or just wait for next refresh. For now, let's refresh to be consistent.
                                refreshLikedVideos()
                            }
                        } else {
                            // If visiting others, just update the like state if it's in their public like list
                            if (likedIndex != -1) {
                                currentLiked[likedIndex] = currentLiked[likedIndex].copy(
                                    isLiked = event.isLiked,
                                    likeCount = event.newLikeCount
                                )
                                _likedVideos.value = currentLiked
                            }
                        }
                    }
                    is InteractionEvent.CommentAdded -> {
                        val currentVideos = _publishedVideos.value.toMutableList()
                        val index = currentVideos.indexOfFirst { it.id == event.videoId }
                        if (index != -1) {
                            currentVideos[index] = currentVideos[index].copy(
                                commentCount = event.newCommentCount
                            )
                            _publishedVideos.value = currentVideos
                        }

                        val currentLiked = _likedVideos.value.toMutableList()
                        val likedIndex = currentLiked.indexOfFirst { it.id == event.videoId }
                        if (likedIndex != -1) {
                            currentLiked[likedIndex] = currentLiked[likedIndex].copy(
                                commentCount = event.newCommentCount
                            )
                            _likedVideos.value = currentLiked
                        }
                    }
                }
            }
        }
    }

    private fun refreshLikedVideos() {
        viewModelScope.launch {
            likedVideoCursor = 0L
            likedHasMore = true
            val result = getFavoriteVideosUseCase(userId, likedVideoCursor)
            if (result is Resource.Success) {
                val (videos, nextCursor) = result.data
                _likedVideos.value = videos
                likedVideoCursor = nextCursor
                likedHasMore = nextCursor > 0
            }
        }
    }

    fun loadMoreLikedVideos() {
        if (!likedHasMore || _pagingState.value is PagingState.Loading) return

        viewModelScope.launch {
            _pagingState.value = PagingState.Loading
            val result = getFavoriteVideosUseCase(userId, likedVideoCursor)
            if (result is Resource.Success) {
                val (newVideos, nextCursor) = result.data
                val currentVideos = _likedVideos.value.toMutableList()
                val existingIds = currentVideos.map { it.id }.toSet()
                val uniqueNewVideos = newVideos.filter { it.id !in existingIds }

                _likedVideos.value = currentVideos + uniqueNewVideos
                likedVideoCursor = nextCursor
                likedHasMore = nextCursor > 0
                _pagingState.value = PagingState.Idle
            } else if (result is Resource.Error) {
                _pagingState.value = PagingState.Error(result.message)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _viewMode.value = if (isSelf()) ProfileViewMode.SELF else ProfileViewMode.VISITOR

            videoCursor = 0L
            hasMore = true

            val videosResult = getPublishedVideosUseCase(userId, videoCursor)
            if (videosResult is Resource.Success) {
                val (videos, nextCursor) = videosResult.data
                _publishedVideos.value = videos
                videoCursor = nextCursor
                hasMore = nextCursor > 0
            }

            refreshLikedVideos()

            when (val result = getProfileInfoUseCase(userId)) {
                is Resource.Success -> {
                    val info = result.data
                    _profileInfo.value = info.copy(
                        workCount = _publishedVideos.value.size,
                        favoritedCount = _publishedVideos.value.sumOf { it.likeCount }
                    )
                }
                else -> _profileInfo.value = null
            }
            _isRefreshing.value = false
            _isLoading.value = false
        }
    }

    fun loadMoreVideos() {
        if (!hasMore || _pagingState.value is PagingState.Loading) return

        viewModelScope.launch {
            _pagingState.value = PagingState.Loading
            val result = getPublishedVideosUseCase(userId, videoCursor)
            if (result is Resource.Success) {
                val (newVideos, nextCursor) = result.data
                val currentVideos = _publishedVideos.value.toMutableList()
                val existingIds = currentVideos.map { it.id }.toSet()
                val uniqueNewVideos = newVideos.filter { it.id !in existingIds }

                _publishedVideos.value = currentVideos + uniqueNewVideos
                videoCursor = nextCursor
                hasMore = nextCursor > 0
                _pagingState.value = PagingState.Idle
            } else if (result is Resource.Error) {
                _pagingState.value = PagingState.Error(result.message)
            }
        }
    }

    fun toggleFollow() {
        val info = _profileInfo.value ?: return
        interactionManager.toggleFollow(info.id, info.isFollowed)
    }

    fun isSelf(): Boolean {
        return userId == null || userId == authManager.getUserId()
    }

    fun logout() {
        authRepository.logout()
    }
}
