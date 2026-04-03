package com.app.douyin.pro.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val _username = MutableStateFlow("南京最帅程序员")
    val username: StateFlow<String> = _username

    private val _douyinId = MutableStateFlow("JulesCode_99")
    val douyinId: StateFlow<String> = _douyinId
}
