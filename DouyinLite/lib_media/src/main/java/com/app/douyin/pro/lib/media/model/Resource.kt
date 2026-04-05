package com.app.douyin.pro.lib.media.model

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val error: AppError? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
