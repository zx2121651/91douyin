package com.app.douyin.pro.feature.edit.ui

import androidx.compose.runtime.Composable
import com.app.douyin.pro.feature.edit.ui.screen.EditScreen as RealEditScreen

import android.net.Uri

@Composable
fun EditScreen(
    videoUri: String = "",
    onClose: () -> Unit = {},
    onNext: (Uri) -> Unit = {}
) {
    RealEditScreen(
        videoUri = videoUri,
        onClose = onClose,
        onNext = onNext
    )
}
