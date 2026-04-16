package com.app.douyin.pro.feature.record.ui.state

data class PublishUiState(
    val videoUri: String = "",
    val title: String = "",
    val isPublishing: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
