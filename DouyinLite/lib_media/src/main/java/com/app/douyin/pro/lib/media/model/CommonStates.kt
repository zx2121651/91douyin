package com.app.douyin.pro.lib.media.model

sealed class PagingState {
    object Idle : PagingState()
    object Loading : PagingState()
    data class Error(val message: String) : PagingState()
}
