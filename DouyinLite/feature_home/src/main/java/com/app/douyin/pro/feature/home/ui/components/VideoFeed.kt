package com.app.douyin.pro.feature.home.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.app.douyin.pro.feature.home.ui.VideoPage
import com.app.douyin.pro.lib.media.model.VideoModel

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.douyin.pro.feature.home.viewmodel.PagingState
import com.app.douyin.pro.lib.media.VideoPlayerManager


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoFeed(
    videos: List<VideoModel>,
    isVisible: Boolean,
    onNavigateToProfile: () -> Unit,
    pagingState: PagingState = PagingState.Idle,
    initialPage: Int = 0,
    onLoadMore: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { videos.size })
    val context = LocalContext.current
    val playerManager = remember { VideoPlayerManager.getInstance(context) }

    // Preload strategy: update the preload list based on current page
    LaunchedEffect(pagerState.currentPage, videos) {
        if (videos.isNotEmpty()) {
            val preloadUrls = videos.subList(
                (pagerState.currentPage + 1).coerceAtMost(videos.size),
                (pagerState.currentPage + 5).coerceAtMost(videos.size)
            ).map { it.playUrl }

            playerManager.updatePreloadList(preloadUrls)
        }

        // Trigger load more when reaching near the end
        if (pagerState.currentPage >= videos.size - 2 && videos.isNotEmpty()) {
            onLoadMore()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

        // Pagination status overlay at the bottom
        if (pagingState !is PagingState.Idle) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // Avoid overlap with bottom bar
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (pagingState) {
                    is PagingState.Loading -> {
                        CircularProgressIndicator(
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    is PagingState.Error -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), MaterialTheme.shapes.small)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "加载更多失败",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = onLoadMore,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.heightIn(min = 24.dp)
                            ) {
                                Text("重试", color = Color(0xFFFF2C55), fontSize = 12.sp)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
