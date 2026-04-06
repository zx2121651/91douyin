package com.app.douyin.pro.lib.media.auth

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("douyin_auth", Context.MODE_PRIVATE)

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun getUserId(): Long {
        return prefs.getLong("user_id", -1L)
    }

    fun saveAuth(token: String, userId: Long) {
        prefs.edit()
            .putString("token", token)
            .putLong("user_id", userId)
            .apply()
    }

    fun clearAuth() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null && getUserId() != -1L
    }
}
