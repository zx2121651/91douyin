package com.app.douyin.pro.feature.home.viewmodel

sealed class PagingState {
    object Idle : PagingState()
    object Loading : PagingState()
    data class Error(val message: String) : PagingState()
}
