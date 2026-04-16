package com.app.douyin.pro.lib.media.auth

import android.util.Log
import com.app.douyin.pro.lib.media.network.DouyinApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SilentLoginInitializer @Inject constructor(
    private val authManager: AuthManager,
    private val apiService: DouyinApiService
) {
    fun initialize() {
        CoroutineScope(Dispatchers.IO).launch {
            if (authManager.isLoggedIn()) {
                val token = authManager.getToken()!!
                val userId = authManager.getUserId()
                Log.d("Auth", "Already logged in with user ID: $userId, refreshing user info...")
                try {
                    val userInfoResponse = apiService.getUserInfo(userId, token)
                    if (userInfoResponse.statusCode == 0 && userInfoResponse.user != null) {
                        authManager.saveAuth(token, userInfoResponse.user)
                        Log.d("Auth", "User info refreshed successfully.")
                    } else {
                        Log.w("Auth", "Failed to refresh user info, token might be invalid: ${userInfoResponse.statusMsg}")
                        authManager.clearAuth()
                    }
                } catch (e: Exception) {
                    Log.e("Auth", "Network error refreshing user info: ${e.message}")
                    // Keep existing session if it's just a network error
                }
                return@launch
            }

            try {
                // Try login first (using a default test user)
                val username = "testuser"
                val password = "testpassword"
                var response = apiService.login(username, password)

                // If login fails (user doesn't exist), register
                if (response.statusCode != 0) {
                    Log.d("Auth", "Login failed, attempting register...")
                    response = apiService.register(username, password)
                }

                if (response.statusCode == 0) {
                    val userInfoResponse = apiService.getUserInfo(response.userId, response.token)
                    if (userInfoResponse.statusCode == 0 && userInfoResponse.user != null) {
                        authManager.saveAuth(response.token, userInfoResponse.user)
                        Log.d("Auth", "Silent login/register successful. UserID: ${response.userId}")
                    } else {
                        Log.e("Auth", "Silent login: Failed to get user info: ${userInfoResponse.statusMsg}")
                        authManager.updateSession(SessionState.Guest)
                    }
                } else {
                    Log.e("Auth", "Silent login: Failed to get token: ${response.statusMsg}")
                    authManager.updateSession(SessionState.Guest)
                }
            } catch (e: Exception) {
                Log.e("Auth", "Network error during silent login: ${e.message}")
                authManager.updateSession(SessionState.Guest)
            }
        }
    }
}
