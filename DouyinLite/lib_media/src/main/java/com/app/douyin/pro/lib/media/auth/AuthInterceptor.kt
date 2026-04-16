package com.app.douyin.pro.lib.media.auth

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authManager: AuthManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val token = authManager.getToken()
        val userId = authManager.getUserId()

        // Many Douyin APIs require token and user_id in the URL query parameters
        if (token != null && userId != -1L) {
            val url = request.url.newBuilder()
                .addQueryParameter("token", token)
                .addQueryParameter("user_id", userId.toString())
                .build()

            request = request.newBuilder()
                .url(url)
                .build()
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            // Centralized 401 handling
            authManager.clearAuth()
        }

        return response
    }
}
