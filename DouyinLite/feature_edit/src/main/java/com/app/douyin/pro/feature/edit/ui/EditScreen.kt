package com.app.douyin.pro.feature.edit.ui

import androidx.compose.runtime.Composable
import com.app.douyin.pro.feature.edit.ui.screen.EditScreen as RealEditScreen

@Composable
fun EditScreen(
    videoUri: String = "",
    onClose: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    RealEditScreen(
        videoUri = videoUri,
        onClose = onClose,
        onNext = onNext
    )
}
