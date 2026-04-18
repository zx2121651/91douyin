package com.app.douyin.pro.feature.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.profile.data.source.ProfileInfo
import com.app.douyin.pro.feature.profile.domain.usecase.GetProfileInfoUseCase
import com.app.douyin.pro.lib.media.model.Resource

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

    private val _publishedVideos = MutableStateFlow<List<VideoModel>>(emptyList())
    val publishedVideos: StateFlow<List<VideoModel>> = _publishedVideos.asStateFlow()

    init {
        viewModelScope.launch {
            sessionState.collect { state ->
                if (state is SessionState.LoggedIn) {
                    loadProfile()
                } else if (userId != null) {
                    // Even if not logged in, we might be able to see some profiles if backend allows
                    // But usually, we follow the current session logic.
                    // For now, if there's a specific userId, we try to load it.
                    loadProfile()
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
                if (event is InteractionEvent.FollowChanged) {
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
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _viewMode.value = if (isSelf()) ProfileViewMode.SELF else ProfileViewMode.VISITOR

            val videosResult = getPublishedVideosUseCase(userId)
            val videos = if (videosResult is Resource.Success) videosResult.data else emptyList()
            _publishedVideos.value = videos

            when (val result = getProfileInfoUseCase(userId)) {
                is Resource.Success -> {
                    _profileInfo.value = result.data.copy(
                        workCount = videos.size,
                        favoritedCount = videos.sumOf { it.likeCount }
                    )
                }
                else -> _profileInfo.value = null
            }
            _isLoading.value = false
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
