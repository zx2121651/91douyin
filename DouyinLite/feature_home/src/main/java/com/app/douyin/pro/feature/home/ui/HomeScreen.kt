@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.app.douyin.pro.feature.home.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.core.Spring

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.foundation.border
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.app.douyin.pro.lib.media.VideoPlayerManager
import androidx.compose.runtime.snapshotFlow
import androidx.media3.common.Player
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.douyin.pro.feature.home.viewmodel.HomeViewModel
import androidx.compose.runtime.collectAsState


@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun HomeScreen(
    onNavigateToMall: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val videos = viewModel.videos
    val isLoading by viewModel.isLoading.collectAsState()

    // Using BeyondBoundsPageCount = 1 to pre-load adjacent pages for smoother scrolling
    val pagerState = rememberPagerState(pageCount = { videos.size })

    var showSearchScreen by remember { mutableStateOf(false) }

    if (showSearchScreen) {
        SearchScreen(onCancel = { showSearchScreen = false })
        return
    }

    // Pagination logic
    LaunchedEffect(pagerState.currentPage, videos.size) {
        // Load more when reaching the 2nd to last item
        if (videos.isNotEmpty() && pagerState.currentPage >= videos.size - 2 && !isLoading) {
            viewModel.loadMore()
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

    val horizontalPagerState = rememberPagerState(initialPage = 3, pageCount = { 4 })
    val selectedTopTabIndex = horizontalPagerState.currentPage + 1 // prepend "商城" tab at index 0
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    LiveScreen()
                }
                1 -> {
                    // 同城骨架
                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                        Text("同城占位内容", color = Color.White, modifier = Modifier.align(Alignment.Center))
                    }
                }
                2 -> {
                    // 关注骨架
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        Text("关注页占位内容", color = Color.White, modifier = Modifier.align(Alignment.Center))
                    }
                }
                3 -> {
                    VideoFeed(
                        videos = videos,
                        isVisible = horizontalPagerState.currentPage == 3,
                        onNavigateToProfile = onNavigateToProfile
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = horizontalPagerState.currentPage != 0,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopNavigationBar(
                selectedTabIndex = selectedTopTabIndex,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        horizontalPagerState.animateScrollToPage(index)
                    }
                },
                onSearchClick = { showSearchScreen = true },
                onNavigateToMall = onNavigateToMall,
                modifier = Modifier.statusBarsPadding()
            )
        }
    }
}

@Composable
fun TopNavigationBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onSearchClick: () -> Unit = {},
    onNavigateToMall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tabs = listOf("商城", "直播", "南京", "关注", "推荐")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "Menu",
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
                val fontSize = if (isSelected) 18.sp else 16.sp
                val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { if (index == 0) onNavigateToMall() else onTabSelected(index - 1) }
                ) {
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = alpha),
                        fontSize = fontSize,
                        fontWeight = fontWeight
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.width(24.dp).height(3.dp).background(Color.White, RoundedCornerShape(1.5.dp)))
                    }
                }
            }
        }

        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = Color.White,
            modifier = Modifier.size(28.dp).clickable { onSearchClick() }
        )
    }
}

@Composable
fun VideoPage(url: String, isVisible: Boolean) {
    val hearts = remember { mutableStateListOf<LikeHeart>() }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showLongPressMenu by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isPaused = !isPaused
                    },
                    onDoubleTap = { offset ->
                        hearts.add(LikeHeart(x = offset.x, y = offset.y))
                    },
                    onLongPress = {
                        showLongPressMenu = true
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




        // Video Player Layer
        VideoPlayer(url = url, isVisible = isVisible, isDucked = showCommentsSheet, isPaused = isPaused)

        androidx.compose.animation.AnimatedVisibility(
            visible = isPaused,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(initialScale = 1.5f),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(targetScale = 1.5f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Paused",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(80.dp)
            )
        }

        // UI Layer
        RightSideActions(
            onCommentClick = { showCommentsSheet = true }, onShareClick = { showShareSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding() // Avoid system nav bar
                .padding(bottom = 100.dp, end = 16.dp)
        )


        if (showCommentsSheet) {
            CommentsBottomSheet(onDismiss = { showCommentsSheet = false })
        }

        if (showShareSheet) {
            ShareBottomSheet(onDismiss = { showShareSheet = false })
        }

        if (showLongPressMenu) {
            LongPressMenu(onDismiss = { showLongPressMenu = false })
        }


        // Bottom Info Layer
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding() // Avoid system nav bar
                .padding(bottom = 60.dp, start = 16.dp, end = 80.dp) // Avoid overlapping with record
        ) {
            // E-commerce Card
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color(0xFF323440).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBJq9n8h2_FF69F_-Y1n0oCj2pnz0mUeXJa2hSTz7b_hjOlIYOHtbKhTcWPQ0JfHzOe8gehtur62cF_wDJ7EwSmnQhFaV8pdnucEgjblq4x_02yPaG5OKB2iDIeEbTm1ZGvcNhUGAioCtXE4QIz_s0yVnbciXqrGfLCMWVkEyXExQpx_vpymOaZRAONXmSCQPqFNQxP8vPZlKjqi42H3Mlsqa74XO0YhDD27e8VaRhzD0nRa3qj-MhiQKntNtBNrE-Jtk3rnI6R6OQ",
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color(0xFF35fbf5),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "查看同款商品",
                            color = Color(0xFF35fbf5),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "新款极速运动鞋 - 限时特惠",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "@潮流先锋官方", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "今日份城市漫步穿搭，极简主义与霓虹色彩的完美融合。#街拍 #潮流 #生活方式", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Music",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.width(160.dp)) {
                    Text(
                        text = "原声 - 潮流先锋官方创作出的音乐",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(url: String, isVisible: Boolean, isDucked: Boolean = false, isPaused: Boolean = false) {
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
fun RightSideActions(onCommentClick: () -> Unit, onShareClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(bottom = 120.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Profile Picture with Follow Button
        var isFollowed by remember { mutableStateOf(false) }
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.padding(bottom = 8.dp)) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAFRJnvPgLJTZNlp2beH3rKkgrIq79yAByHrNztp31d3S5Ql5HDcVsXOtOffLNhtuX4qaajnkwFgdAFL5OCuwdLzNBs9QDqqeiJejfbJPzXVeArU5eX10395R9he1IM-Eoy2kh6lmFA_v6n8auwbHfT6iBKAZdZODWoz0wWWJn57dDE7AybZhChYpQ6vVgt7ESF1A6VaNFSrjxMK6MuHftCkoxICASpEx6ooT2VDLv3mlsVbLQNXGa1uCeoOWCamXI699HkQHUvmOk",
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = !isFollowed,
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
                modifier = Modifier.offset(y = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Follow",
                    tint = Color(0xFFFF2C55),
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { isFollowed = true }
                )
            }
        }

        // Interactive Buttons
        LikeButton()
        CommentButton(onClick = onCommentClick)
        FavoriteButton()
        ShareButton(onClick = onShareClick)

        // Rotating Music Disc
        Box {
            FloatingMusicNotes()
            SpinningRecord()
        }
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

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Like",
            tint = if (isLiked) Color(0xFFFF2C55) else Color.White,
            modifier = Modifier
                .size(40.dp)
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isLiked = !isLiked
                }
        )
        Text(text = "128.4w", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun CommentButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            onClick()
        }
    ) {
        Icon(
            imageVector = Icons.Filled.MailOutline,
            contentDescription = "Comment",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
        Text(text = "4.2w", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun FavoriteButton() {
    var isFavorited by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Favorite",
            tint = if (isFavorited) Color(0xFFFFD700) else Color.White,
            modifier = Modifier
                .size(40.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isFavorited = !isFavorited
                }
        )
        Text(text = "2.1w", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun ShareButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = "Share",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
        Text(text = "8.5w", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF161823),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                "分享到",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                val shares = listOf("私信", "群聊", "朋友圈", "微信", "QQ", "复制链接")
                items(shares) { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF2E2E2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}


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
                detectHorizontalDragGestures { change, dragAmount ->
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

@Composable
fun LongPressMenu(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .background(Color(0xFF2E2E2E), RoundedCornerShape(16.dp))
                .padding(vertical = 8.dp)
        ) {
            val options = listOf("不感兴趣", "保存视频", "收藏", "举报")
            options.forEach { option ->
                Text(
                    text = option,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
                if (option != options.last()) {
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 0.5.dp)
                }
            }
        }
    }
}
