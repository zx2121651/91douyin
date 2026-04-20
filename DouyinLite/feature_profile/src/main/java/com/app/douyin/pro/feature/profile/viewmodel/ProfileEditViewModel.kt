package com.app.douyin.pro.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.douyin.pro.feature.profile.data.ProfileRepository
import com.app.douyin.pro.feature.profile.data.source.ProfileInfo
import com.app.douyin.pro.feature.profile.domain.usecase.GetProfileInfoUseCase
import com.app.douyin.pro.lib.media.interaction.VideoInteractionManager
import com.app.douyin.pro.lib.media.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val getProfileInfoUseCase: GetProfileInfoUseCase,
    private val repository: ProfileRepository,
    private val interactionManager: VideoInteractionManager
) : ViewModel() {

    private val _profileInfo = MutableStateFlow<ProfileInfo?>(null)
    val profileInfo: StateFlow<ProfileInfo?> = _profileInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveStatus = MutableStateFlow<Resource<Unit>?>(null)
    val saveStatus: StateFlow<Resource<Unit>?> = _saveStatus.asStateFlow()

    // Form State
    val nickname = MutableStateFlow("")
    val signature = MutableStateFlow("")

    // Bytes for upload
    var avatarBytes: ByteArray? = null
    var backgroundBytes: ByteArray? = null

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = getProfileInfoUseCase(null)) {
                is Resource.Success -> {
                    _profileInfo.value = result.data
                    nickname.value = result.data.username
                    signature.value = result.data.signature ?: ""
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _saveStatus.value = Resource.Loading

            val result = repository.updateProfile(
                name = nickname.value,
                signature = signature.value,
                avatarBytes = avatarBytes,
                backgroundBytes = backgroundBytes
            )

            _saveStatus.value = result
            if (result is Resource.Success) {
                interactionManager.notifyProfileUpdated()
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = null
    }
}
