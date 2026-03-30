@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.app.douyin.pro.feature.home.ui

import androidx.compose.animation.core.Spring

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement

import androidx.compose.ui.Alignment

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.Divider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.sin
import kotlin.math.PI
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.random.Random
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import java.util.UUID
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf

import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.HorizontalDivider
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.app.douyin.pro.lib.media.VideoPlayerManager
import androidx.compose.runtime.snapshotFlow
import androidx.media3.common.Player
import androidx.compose.ui.layout.ContentScale

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun HomeScreen() {
    val initialVideos = remember { MockData.videos }
    val videos = remember { mutableStateListOf<String>().apply { addAll(initialVideos) } }
    var isLoading by remember { mutableStateOf(false) }
    var pageCount by remember { mutableIntStateOf(1) }

    // Using BeyondBoundsPageCount = 1 to pre-load adjacent pages for smoother scrolling
    val pagerState = rememberPagerState(pageCount = { videos.size })

    // Pagination logic
    LaunchedEffect(pagerState.currentPage) {
        // Load more when reaching the 2nd to last item
        if (pagerState.currentPage >= videos.size - 2 && !isLoading) {
            isLoading = true
            val newVideos = MockData.loadMoreVideos(pageCount)
            videos.addAll(newVideos)
            pageCount++
            isLoading = false
        }
    }


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

    val horizontalPagerState = rememberPagerState(initialPage = 2, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    // 同城骨架
                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                        Text("同城占位内容", color = Color.White, modifier = Modifier.align(Alignment.Center))
                    }
                }
                1 -> {
                    // 关注骨架
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        Text("关注页占位内容", color = Color.White, modifier = Modifier.align(Alignment.Center))
                    }
                }
                2 -> {
                    // 推荐视频流
                    VerticalPager(
                        state = pagerState,
                        beyondBoundsPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { vPage ->
                        val isVisible = pagerState.currentPage == vPage && horizontalPagerState.currentPage == 2
                        VideoPage(
                            url = videos[vPage],
                            isVisible = isVisible
                        )
                    }
                }
            }
        }

        TopNavigationBar(
            selectedTabIndex = horizontalPagerState.currentPage,
            onTabSelected = { index ->
                coroutineScope.launch {
                    horizontalPagerState.animateScrollToPage(index)
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )
    }
}

@Composable
fun TopNavigationBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("同城", "关注", "推荐")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTabIndex
                val alpha by animateFloatAsState(targetValue = if (isSelected) 1f else 0.7f, label = "alpha")
                val fontSize = if (isSelected) MaterialTheme.typography.titleLarge.fontSize else MaterialTheme.typography.titleMedium.fontSize
                val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onTabSelected(index) }
                ) {
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = alpha),
                        fontSize = fontSize,
                        fontWeight = fontWeight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(3.dp)
                                .background(Color.White, shape = CircleShape)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }
        }

        // Placeholder to balance the row since search is on the left
        Box(modifier = Modifier.size(28.dp))
    }
}

@Composable
fun VideoPage(url: String, isVisible: Boolean) {
    val hearts = remember { mutableStateListOf<LikeHeart>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        hearts.add(LikeHeart(x = offset.x, y = offset.y))
                    }
                )
            }
    ) {


        // Render double-tap hearts
        hearts.forEach { heart ->
            AnimatedHeart(
                heart = heart,
                onRemove = { hearts.remove(heart) }
            )
        }

        var showCommentsSheet by remember { mutableStateOf(false) }

        // Video Player Layer
        VideoPlayer(url = url, isVisible = isVisible, isDucked = showCommentsSheet)

        // UI Layer
        RightSideActions(
            onCommentClick = { showCommentsSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding() // Avoid system nav bar
                .padding(bottom = 100.dp, end = 16.dp)
        )

        if (showCommentsSheet) {
            CommentsBottomSheet(onDismiss = { showCommentsSheet = false })
        }

        // Bottom Info Layer
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding() // Avoid system nav bar
                .padding(bottom = 60.dp, start = 16.dp, end = 80.dp) // Avoid overlapping with record
        ) {
            Text(text = "@User_${url.hashCode()}", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "This is a beautiful video #amazing #fyp", color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🎵",
                    color = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Original Audio - @User_${url.hashCode()} - Popular Trending Song 2024",
                    color = Color.White,
                    modifier = Modifier.basicMarquee()
                )
            }
        }
    }
}

@Composable
fun VideoPlayer(url: String, isVisible: Boolean, isDucked: Boolean = false) {
    val context = LocalContext.current
    val playerManager = remember { VideoPlayerManager.getInstance(context) }

    // Manage player lifecycle and visibility
    val player = remember(url) { playerManager.getPlayer(url) }

    // Progress States
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(1L) }
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    // Poll current playback position
    LaunchedEffect(player, isVisible, isDragging) {
        if (isVisible && !isDragging) {
            while (isActive) {
                currentPosition = player.currentPosition
                val dur = player.duration
                if (dur > 0) {
                    duration = dur
                }
                delay(50)
            }
        }
    }

    var isVideoReady by remember { mutableStateOf(false) }

    // Listener for video ready state
    DisposableEffect(player) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == androidx.media3.common.Player.STATE_READY) {
                    isVideoReady = true
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
                player.pause()
            } else if (event == Lifecycle.Event.ON_RESUME && isVisible) {
                player.play()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isDucked) {
        if (isDucked) {
            player.volume = 0.3f
        } else {
            player.volume = 1.0f
        }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            // EXOPlayer setAudioAttributes handles audio focus automatically internally
            val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()
            player.setAudioAttributes(audioAttributes, true)
            player.play()
        } else {
            player.pause()
            player.seekTo(0) // Precise lifecycle hook: Reset to 0 when unselected
        }
    }

    DisposableEffect(url) {
        onDispose {
            playerManager.releasePlayer(url)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                // Using SurfaceView implicitly via PlayerView, optimal for power consumption
                androidx.media3.ui.PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    // Ensure we use SurfaceView (default behavior of PlayerView if not overridden in XML)
                    // If XML specifies textureView, programmatically setting it here is more complex without inflation.
                    // By default, PlayerView inflates exo_player_view.xml which uses a SurfaceView.
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Placeholder and Alpha Animation
        // Since we don't have real thumbnails in MockData, we generate a synthetic one or use a dark placeholder
        // Using an AnimatedVisibility or animateFloatAsState for the alpha transition (300ms)
        val placeholderAlpha by animateFloatAsState(
            targetValue = if (isVideoReady) 0f else 1f,
            animationSpec = tween(durationMillis = 300),
            label = "placeholderAlpha"
        )

        if (placeholderAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF161823).copy(alpha = placeholderAlpha)) // Douyin dark theme surface color
            ) {
                // Here we would use coil AsyncImage if we had a thumbnail URL:
                // AsyncImage(model = thumbnailUrl, contentDescription = null, contentScale = ContentScale.Crop)
            }
        }

        // Progress computation
        val progress = if (isDragging) dragProgress else {
            if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
        }

        // Custom progress bar overlay at the bottom
        VideoProgressBar(
            progress = progress,
            isDragging = isDragging,
            onDragStart = {
                isDragging = true
                player.pause()
            },
            onDrag = { newProgress ->
                dragProgress = newProgress.coerceIn(0f, 1f)
            },
            onDragEnd = {
                isDragging = false
                player.seekTo((dragProgress * duration).toLong())
                player.play()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )

        // Center popup during drag
        if (isDragging) {
            CenterTimePopup(
                currentTime = (dragProgress * duration).toLong(),
                totalTime = duration,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun RightSideActions(onCommentClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LikeButton()
        Spacer(modifier = Modifier.height(16.dp))
        CommentButton(onClick = onCommentClick)
        Spacer(modifier = Modifier.height(16.dp))
        Box {
            FloatingMusicNotes()
            SpinningRecord()
        }
    }
}

@Composable
fun CommentButton(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.MailOutline,
            contentDescription = "Comment",
            tint = Color.White,
            modifier = Modifier
                .size(40.dp)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    onClick()
                }
        )
        Text(text = "128", color = Color.White, style = MaterialTheme.typography.labelSmall)
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
        tint = if (isLiked) Color(0xFFFFD700) else Color.White,
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

data class LikeHeart(val id: String = UUID.randomUUID().toString(), val x: Float, val y: Float)

@Composable
fun AnimatedHeart(heart: LikeHeart, onRemove: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    val yOffset = remember { Animatable(0f) }
    val rotation = remember { Random.nextInt(-15, 15).toFloat() }

    LaunchedEffect(heart) {
        launch {
            scale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(100)
            )
        }

        delay(300) // Stay visible for a short time

        launch {
            yOffset.animateTo(
                targetValue = -100f,
                animationSpec = tween(500)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(500)
            )
        }

        delay(500) // Wait for fade out to complete
        onRemove()
    }

    Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = null,
        tint = Color.Red,
        modifier = Modifier
            .offset { IntOffset(heart.x.toInt() - 100, heart.y.toInt() - 100 + yOffset.value.toInt()) }
            .size(80.dp)
            .scale(scale.value)
            .rotate(rotation)
            .alpha(alpha.value)
    )
}

@Composable
fun SpinningRecord() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .size(50.dp)
            .rotate(rotation)
            .background(Color.DarkGray, CircleShape)
            .padding(8.dp)
            .background(Color.LightGray, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Music",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun FloatingMusicNotes() {
    val infiniteTransition = rememberInfiniteTransition()

    // Create 3 notes with different delays
    val note1Progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart)
    )

    val note2Progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, delayMillis = 800, easing = LinearEasing), RepeatMode.Restart)
    )

    val note3Progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, delayMillis = 1500, easing = LinearEasing), RepeatMode.Restart)
    )

    Box(modifier = Modifier.width(60.dp).height(120.dp)) {
        MusicNote(note1Progress, startX = 10f)
        MusicNote(note2Progress, startX = 25f)
        MusicNote(note3Progress, startX = 40f)
    }
}

@Composable
fun MusicNote(progress: Float, startX: Float) {
    if (progress == 0f) return

    val yOffset = -200f * progress
    val xOffset = startX + 30f * sin(progress * 4f * PI).toFloat()

    val alpha = if (progress < 0.2f) {
        progress * 5f // Fade in
    } else if (progress > 0.8f) {
        (1f - progress) * 5f // Fade out
    } else {
        1f
    }

    Text(
        text = "🎵",
        color = Color.White.copy(alpha = alpha),
        modifier = Modifier
            .offset(x = xOffset.dp, y = yOffset.dp)
            .size(16.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun VideoProgressBar(
    progress: Float,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barHeight by animateDpAsState(if (isDragging) 4.dp else 1.dp, label = "BarHeight")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp) // Touch target height
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        onDragStart()
                        onDrag(offset.x / size.width)
                        if (tryAwaitRelease()) {
                            onDragEnd()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        onDragStart()
                        onDrag(offset.x / size.width)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                    onHorizontalDrag = { change, dragAmount ->
                        val newX = change.position.x
                        onDrag((newX / size.width).coerceIn(0f, 1f))
                    }
                )
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(barHeight)) {
            val width = size.width
            val height = size.height

            // Background track (semi-transparent)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                size = Size(width, height),
                cornerRadius = CornerRadius(height / 2, height / 2)
            )

            // Progress track (solid white)
            val progressWidth = width * progress
            drawRoundRect(
                color = Color.White,
                size = Size(progressWidth, height),
                cornerRadius = CornerRadius(height / 2, height / 2)
            )

            // Thumb
            if (isDragging) {
                drawCircle(
                    color = Color.White,
                    radius = height * 1.5f,
                    center = Offset(progressWidth, height / 2)
                )
            }
        }
    }
}

@Composable
fun CenterTimePopup(currentTime: Long, totalTime: Long, modifier: Modifier = Modifier) {
    val currentFormatted = formatTime(currentTime)
    val totalFormatted = formatTime(totalTime)

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "$currentFormatted / $totalFormatted",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        },
        containerColor = Color(0x99000000), // Glassmorphism translucent dark
        scrimColor = Color.Transparent, // Avoid completely darkening the video behind
        modifier = Modifier
            .fillMaxHeight(0.7f)
            .imePadding() // Pushes up when the keyboard opens
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "128 条评论",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(20) { index ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "User $index", color = Color(0xFFC0C0C0), style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "This is a wonderful video! Really love the content here.", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("留下你的精彩评论...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.8f),
                        unfocusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.8f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
