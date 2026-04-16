package com.app.douyin.pro.lib.media.auth

import com.app.douyin.pro.lib.media.network.model.UserDto

sealed class SessionState {
    object Initializing : SessionState()
    object Guest : SessionState()
    data class LoggedIn(val user: UserDto) : SessionState()
    data class Error(val message: String) : SessionState()
}
