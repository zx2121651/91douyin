package com.app.douyin.pro.lib.media.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    // 简化处理：实际中应当从 DataStore/SharedPreferences 中读取 Token
    private var token: String = ""

    fun setToken(newToken: String) {
        token = newToken
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.newBuilder()
            .apply {
                if (token.isNotEmpty()) {
                    addQueryParameter("token", token)
                }
            }
            .build()
        val newRequest = request.newBuilder().url(url).build()
        return chain.proceed(newRequest)
    }
}
