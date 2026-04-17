package com.app.douyin.pro.lib.media.interaction

import com.app.douyin.pro.lib.media.auth.AuthManager
import com.app.douyin.pro.lib.media.network.DouyinApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageUnreadManager @Inject constructor(
    private val apiService: DouyinApiService,
    private val authManager: AuthManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount.asStateFlow()

    init {
        scope.launch {
            authManager.isLoggedInFlow.collectLatest { isLoggedIn ->
                if (isLoggedIn) {
                    startPolling()
                } else {
                    _totalUnreadCount.value = 0
                }
            }
        }
    }

    private fun startPolling() {
        scope.launch {
            while (isActive && authManager.isLoggedIn()) {
                refreshUnreadCount()
                delay(10000) // Poll every 10 seconds
            }
        }
    }

    fun refreshUnreadCount() {
        scope.launch {
            try {
                if (authManager.isLoggedIn()) {
                    val token = authManager.requireToken()
                    val response = apiService.getUnreadCount(token)
                    if (response.statusCode == 0) {
                        _totalUnreadCount.value = response.unreadCount.toInt()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
