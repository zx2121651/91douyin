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
        if (authManager.isLoggedIn()) {
            Log.d("Auth", "Already logged in with user ID: ${authManager.getUserId()}")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
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
                    authManager.saveAuth(response.token, response.userId)
                    Log.d("Auth", "Silent login/register successful. UserID: ${response.userId}")
                } else {
                    Log.e("Auth", "Failed to get token: ${response.statusMsg}")
                }
            } catch (e: Exception) {
                Log.e("Auth", "Network error during silent login: ${e.message}")
            }
        }
    }
}
