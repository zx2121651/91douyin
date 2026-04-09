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
import com.app.douyin.pro.feature.home.domain.model.VideoModel

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.app.douyin.pro.lib.media.VideoPlayerManager


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoFeed(
    videos: List<VideoModel>,
    isVisible: Boolean,
    onNavigateToProfile: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { videos.size })
    val context = LocalContext.current
    val playerManager = remember { VideoPlayerManager.getInstance(context) }

    // Preload next 3 videos to ensure Zero First-Frame Delay
    LaunchedEffect(pagerState.currentPage) {
        val nextIndex = pagerState.currentPage + 1
        for (i in nextIndex..minOf(nextIndex + 2, videos.size - 1)) {
            playerManager.preLoad(videos[i].playUrl)
        }
    }

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
            video = videos[vPage],
            isVisible = isVisible && pagerState.currentPage == vPage
        )
    }
}
