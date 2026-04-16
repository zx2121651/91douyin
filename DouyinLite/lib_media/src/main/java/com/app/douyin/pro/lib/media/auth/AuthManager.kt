package com.app.douyin.pro.lib.media.auth

import android.content.Context
import android.content.SharedPreferences
import com.app.douyin.pro.lib.media.network.model.UserDto
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("douyin_auth", Context.MODE_PRIVATE)

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Initializing)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    init {
        val token = getToken()
        val userId = getUserId()
        val userJson = prefs.getString("user_info", null)

        if (token != null && userId != -1L && userJson != null) {
            try {
                val user = gson.fromJson(userJson, UserDto::class.java)
                _sessionState.value = SessionState.LoggedIn(user)
            } catch (e: Exception) {
                _sessionState.value = SessionState.Guest
            }
        } else {
            _sessionState.value = SessionState.Guest
        }
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    /**
     * Returns the current token or throws an IllegalStateException if not logged in.
     * Use this in authenticated-only data sources.
     */
    fun requireToken(): String {
        return getToken() ?: throw IllegalStateException("User is not logged in")
    }

    fun getUserId(): Long {
        return prefs.getLong("user_id", -1L)
    }

    /**
     * Returns the currently logged in user info, or null if guest.
     */
    val currentUser: UserDto?
        get() = (sessionState.value as? SessionState.LoggedIn)?.user

    /**
     * Provides a Flow of the current user, emitting null when logged out.
     */
    val currentUserFlow = sessionState.map { state ->
        (state as? SessionState.LoggedIn)?.user
    }

    /**
     * Provides a Flow of the login status.
     */
    val isLoggedInFlow = sessionState.map { it is SessionState.LoggedIn }

    fun saveAuth(token: String, user: UserDto) {
        val userJson = gson.toJson(user)
        prefs.edit()
            .putString("token", token)
            .putLong("user_id", user.id)
            .putString("user_info", userJson)
            .apply()
        _sessionState.value = SessionState.LoggedIn(user)
    }

    fun updateSession(state: SessionState) {
        _sessionState.value = state
    }

    fun clearAuth() {
        prefs.edit().clear().apply()
        _sessionState.value = SessionState.Guest
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null && getUserId() != -1L
    }
}
