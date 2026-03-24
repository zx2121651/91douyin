package com.app.douyin.pro.feature.home.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.app.douyin.pro.lib.media.VideoPlayerManager
import androidx.compose.runtime.snapshotFlow

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun HomeScreen() {
    val videos = MockData.videos

    // Using BeyondBoundsPageCount = 1 to pre-load adjacent pages for smoother scrolling
    val pagerState = rememberPagerState(pageCount = { videos.size })

    // Observe pager changes for preloading
    val context = LocalContext.current
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
             val playerManager = VideoPlayerManager.getInstance(context)
             // Preload next video
             if (page + 1 < videos.size) {
                 playerManager.preLoad(videos[page + 1])
             }
             // Preload previous video
             if (page - 1 >= 0) {
                 playerManager.preLoad(videos[page - 1])
             }
        }
    }

    VerticalPager(
        state = pagerState,
        beyondBoundsPageCount = 1,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val isVisible = pagerState.currentPage == page
        VideoPage(
            url = videos[page],
            isVisible = isVisible
        )
    }
}

@Composable
fun VideoPage(url: String, isVisible: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video Player Layer
        VideoPlayer(url = url, isVisible = isVisible)

        // UI Layer
        RightSideActions(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding() // Avoid system nav bar
                .padding(bottom = 100.dp, end = 16.dp)
        )

        // Bottom Info Layer
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding() // Avoid system nav bar
                .padding(bottom = 60.dp, start = 16.dp)
        ) {
            Text(text = "@User_$url.hashCode()", color = Color.White)
            Text(text = "This is a beautiful video #amazing #fyp", color = Color.White)
        }
    }
}

@Composable
fun VideoPlayer(url: String, isVisible: Boolean) {
    val context = LocalContext.current
    val playerManager = remember { VideoPlayerManager.getInstance(context) }

    // Manage player lifecycle and visibility
    val player = remember(url) { playerManager.getPlayer(url) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            player.play()
        } else {
            player.pause()
            // Seek to 0 only if you want it to restart, but pause is immediate
        }
    }

    DisposableEffect(url) {
        onDispose {
            playerManager.releasePlayer(url)
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun RightSideActions(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        LikeButton()
    }
}

@Composable
fun LikeButton() {
    var isLiked by remember { mutableStateOf(false) }

    // 弹性动画模拟抖音手感
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "like_scale"
    )

    Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = "Like",
        tint = if (isLiked) Color.Red else Color.White,
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isLiked = !isLiked
            }
    )
}
