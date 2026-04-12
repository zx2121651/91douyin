package com.app.douyin.pro.feature.home.ui.components

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.app.douyin.pro.lib.media.VideoPlayerManager

@Composable
fun VideoPlayerComponent(
    url: String,
    coverUrl: String = "",
    isVisible: Boolean,
    isDucked: Boolean = false,
    isPaused: Boolean = false
) {
    val context = LocalContext.current
    val playerManager = remember { VideoPlayerManager.getInstance(context) }
    val player = remember(url) { playerManager.getPlayer(url) }

    var isFirstFrameRendered by remember { mutableStateOf(false) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                isFirstFrameRendered = true
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            playerManager.releasePlayer(url)
        }
    }

    LaunchedEffect(isVisible, isDucked, isPaused) {
        if (isVisible && !isDucked && !isPaused) {
            player.play()
        } else {
            player.pause()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = !isFirstFrameRendered,
            exit = fadeOut(animationSpec = tween(durationMillis = 300)),
            modifier = Modifier.fillMaxSize()
        ) {
            if (coverUrl.isNotEmpty()) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "Video Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            }
        }

        AnimatedVisibility(
            visible = isPaused,
            enter = fadeIn() + scaleIn(initialScale = 1.5f),
            exit = fadeOut() + scaleOut(targetScale = 1.5f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Paused",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
        }
    }
}
