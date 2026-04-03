package com.app.douyin.pro.feature.home.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.app.douyin.pro.feature.home.ui.VideoPage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoFeed(
    videos: List<String>,
    isVisible: Boolean,
    onNavigateToProfile: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { videos.size })

    VerticalPager(
        state = pagerState,
        beyondBoundsPageCount = 1,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -30f) {
                        onNavigateToProfile()
                    }
                }
            }
    ) { vPage ->
        VideoPage(
            url = videos[vPage],
            isVisible = isVisible && pagerState.currentPage == vPage
        )
    }
}
