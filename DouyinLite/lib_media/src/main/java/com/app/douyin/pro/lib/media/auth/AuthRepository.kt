package com.app.douyin.pro.lib.media.auth

import com.app.douyin.pro.lib.media.network.DouyinApiService
import com.app.douyin.pro.lib.media.network.model.AuthResponse
import com.app.douyin.pro.lib.media.network.model.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) {
    suspend fun login(username: String, password: String): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(username, password)
            if (response.statusCode == 0) {
                val userInfoResponse = apiService.getUserInfo(response.userId, response.token)
                if (userInfoResponse.statusCode == 0 && userInfoResponse.user != null) {
                    authManager.saveAuth(response.token, userInfoResponse.user)
                    Result.success(userInfoResponse.user)
                } else {
                    Result.failure(Exception(userInfoResponse.statusMsg ?: "Failed to get user info"))
                }
            } else {
                Result.failure(Exception(response.statusMsg ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, password: String): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(username, password)
            if (response.statusCode == 0) {
                val userInfoResponse = apiService.getUserInfo(response.userId, response.token)
                if (userInfoResponse.statusCode == 0 && userInfoResponse.user != null) {
                    authManager.saveAuth(response.token, userInfoResponse.user)
                    Result.success(userInfoResponse.user)
                } else {
                    Result.failure(Exception(userInfoResponse.statusMsg ?: "Failed to get user info"))
                }
            } else {
                Result.failure(Exception(response.statusMsg ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        authManager.clearAuth()
    }

    fun getSessionState() = authManager.sessionState
}
