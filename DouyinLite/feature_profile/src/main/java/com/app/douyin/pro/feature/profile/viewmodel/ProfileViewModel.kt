package com.app.douyin.pro.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.profile.data.source.ProfileInfo
import com.app.douyin.pro.feature.profile.domain.usecase.GetProfileInfoUseCase
import com.app.douyin.pro.lib.media.model.Resource

import com.app.douyin.pro.feature.profile.domain.usecase.GetPublishedVideosUseCase
import com.app.douyin.pro.lib.media.network.model.VideoDto

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileInfoUseCase: GetProfileInfoUseCase,
    private val getPublishedVideosUseCase: GetPublishedVideosUseCase
) : ViewModel() {
    private val _profileInfo = MutableStateFlow<ProfileInfo?>(null)
    val profileInfo: StateFlow<ProfileInfo?> = _profileInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _publishedVideos = MutableStateFlow<List<VideoDto>>(emptyList())
    val publishedVideos: StateFlow<List<VideoDto>> = _publishedVideos.asStateFlow()


    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getProfileInfoUseCase()) {
                is Resource.Success -> _profileInfo.value = result.data
                else -> {}
            }
            when (val result = getPublishedVideosUseCase()) {
                is Resource.Success -> _publishedVideos.value = result.data
                else -> {}
            }
            _isLoading.value = false
        }
    }
}
