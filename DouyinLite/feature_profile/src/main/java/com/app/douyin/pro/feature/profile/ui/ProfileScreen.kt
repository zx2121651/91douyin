package com.app.douyin.pro.feature.profile.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.douyin.pro.feature.profile.viewmodel.ProfileViewModel
import com.app.douyin.pro.feature.profile.viewmodel.ProfileViewMode
import com.app.douyin.pro.lib.media.model.PagingState
import com.app.douyin.pro.lib.media.auth.SessionState
import com.app.douyin.pro.lib.media.util.CountFormatter
import kotlin.random.Random
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onVideoClick: ((index: Int) -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val sessionState by viewModel.sessionState.collectAsState()
    val profileInfo by viewModel.profileInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pagingState by viewModel.pagingState.collectAsState()
    val publishedVideos by viewModel.publishedVideos.collectAsState()
    val likedVideos by viewModel.likedVideos.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()

    val username = profileInfo?.username ?: "加载中..."
    val douyinId = profileInfo?.douyinId ?: ""
    val followers = profileInfo?.followers ?: "0"
    val following = profileInfo?.following?.toString() ?: "0"
    val likes = profileInfo?.favoritedCount?.let { CountFormatter.format(it) } ?: "0"
    val signature = profileInfo?.signature?.takeIf { it.isNotEmpty() } ?: "专注 Android 性能优化与 Jetpack Compose 动效开发。\n不写 Bug，只写诗。✨"
    val avatar = profileInfo?.avatar?.takeIf { it.isNotEmpty() } ?: "https://lh3.googleusercontent.com/aida-public/AB6AXuAFRJnvPgLJTZNlp2beH3rKkgrIq79yAByHrNztp31d3S5Ql5HDcVsXOtOffLNhtuX4qaajnkwFgdAFL5OCuwdLzNBs9QDqqeiJejfbJPzXVeArU5eX10395R9he1IM-Eoy2kh6lmFA_v6n8auwbHfT6iBKAZdZODWoz0wWWJn57dDE7AybZhChYpQ6vVgt7ESF1A6VaNFSrjxMK6MuHftCkoxICASpEx6ooT2VDLv3mlsVbLQNXGa1uCeoOWCamXI699HkQHUvmOk"
    val backgroundImage = profileInfo?.backgroundImage

    val darkBg = Color(0xFF161823)
    val grayText = Color(0xFF8E8E93)

    if (sessionState is SessionState.Guest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "登录后查看个人主页", color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2C55)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("去登录", color = Color.White)
                }
            }
        }
        return
    }

    if (isLoading && profileInfo == null) {
        Box(modifier = Modifier.fillMaxSize().background(darkBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFFD444))
        }
        return
    }

    val topBarHeight = 56.dp
    val topBarHeightPx = with(LocalDensity.current) { topBarHeight.toPx() }

    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var scrollOffset by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = scrollOffset + delta

                // Allow scrolling the header up to max its height minus the top bar
                val minOffset = -(headerHeightPx - topBarHeightPx)
                val maxOffset = 0f

                val clampedOffset = newOffset.coerceIn(minOffset, maxOffset)
                val consumed = clampedOffset - scrollOffset
                scrollOffset = clampedOffset

                return if (consumed != 0f) Offset(0f, consumed) else Offset.Zero
            }
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
            .pullRefresh(pullRefreshState)
            .nestedScroll(nestedScrollConnection)
    ) {
        // Background Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .graphicsLayer {
                    translationY = scrollOffset * 0.5f
                }
        ) {
            if (backgroundImage != null) {
                AsyncImage(
                    model = backgroundImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2E2E2E)))
            }
        }

        // Sticky Header / Collapsing logic
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, scrollOffset.roundToInt()) }
                .onGloballyPositioned { coordinates ->
                    if (headerHeightPx == 0f) {
                        headerHeightPx = coordinates.size.height.toFloat()
                    }
                }
        ) {
            // Invisible spacer for top bar placeholder within the scrolling header
            Spacer(modifier = Modifier.height(100.dp)) // Pull down content

            // Profile Header Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkBg, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .padding(horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )
                        AsyncImage(
                            model = avatar,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2C55))
                                .border(2.dp, darkBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(likes, "获赞")
                        StatItem(following, "关注")
                        StatItem(followers, "粉丝")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = username,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "抖音号: $douyinId",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = signature,
                    color = Color.White,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                ProfileActionButtons(
                    viewMode = viewMode,
                    isFollowed = profileInfo?.isFollowed ?: false,
                    onToggleFollow = { viewModel.toggleFollow() },
                    onLogout = { viewModel.logout() },
                    onNavigateToEditProfile = onNavigateToEditProfile
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // The Scrollable content (Tabs + Grid)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = headerHeightPx + scrollOffset
                }
        ) {
            // Tab Row
            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf("作品 ${publishedVideos.size}", "喜欢 ${likedVideos.size}")

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = darkBg, // Ensure it covers the background when sticky
                contentColor = Color.White,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFFFD444),
                        height = 2.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 15.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) Color.White else grayText
                            )
                        }
                    )
                }
            }

            // Waterfall Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 120.dp, start = 1.dp, end = 1.dp, top = 1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val currentList = if (selectedTab == 0) publishedVideos else likedVideos
                val isSelf = viewMode == ProfileViewMode.SELF
                val isFavoritePublic = profileInfo?.favoritePublic ?: false

                if (selectedTab == 1 && !isSelf && !isFavoritePublic) {
                    item(span = { GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "喜欢列表已隐藏",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else if (currentList.isEmpty() && !isLoading && !isRefreshing) {
                    item(span = { GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (selectedTab == 0) "还没有发布过作品" else "还没有喜欢的视频",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                items(currentList.size) { index ->
                    val video = currentList[index]

                    // Load more trigger
                    if (selectedTab == 0 && index == currentList.size - 1) {
                        LaunchedEffect(Unit) {
                            viewModel.loadMoreVideos()
                        }
                    } else if (selectedTab == 1 && index == currentList.size - 1) {
                        LaunchedEffect(Unit) {
                            viewModel.loadMoreLikedVideos()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .background(Color(0xFF2E2E2E))
                            .clickable { onVideoClick?.invoke(index) },
                        contentAlignment = Alignment.BottomStart
                    ) {
                        AsyncImage(
                            model = video.coverUrl,
                            contentDescription = "Video Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Status Badge for Processing
                        if (video.status == "processing") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "审核中",
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val favStr = if (video.likeCount >= 10000) String.format("%.1fw", video.likeCount / 10000.0) else video.likeCount.toString()
                            Text(
                                text = favStr,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Loading more indicator
                if (pagingState is PagingState.Loading) {
                    item(span = { GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFFFD444)
                            )
                        }
                    }
                }

                // Paging Error State
                if (pagingState is PagingState.Error) {
                    item(span = { GridItemSpan(3) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable { viewModel.loadMoreVideos() },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = (pagingState as PagingState.Error).message,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "点击重试",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Top Toolbar (Always on top)
        // Background fades in as we scroll up
        val topBarAlpha = if (headerHeightPx > 0) {
            val minOffset = -(headerHeightPx - topBarHeightPx)
            (scrollOffset / minOffset).coerceIn(0f, 1f)
        } else {
            0f
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = Color.White,
            contentColor = Color(0xFFFF2C55)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkBg.copy(alpha = topBarAlpha))
                .statusBarsPadding()
                .height(topBarHeight)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (viewMode == ProfileViewMode.SELF) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
            } else {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.clickable { onBack?.invoke() }
                )
            }

            // Name fades in when toolbar collapses
            Text(
                text = username,
                color = Color.White.copy(alpha = topBarAlpha),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White)
                Icon(Icons.Filled.Menu, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun ProfileActionButtons(
    viewMode: ProfileViewMode,
    isFollowed: Boolean,
    onToggleFollow: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToEditProfile: (() -> Unit)? = null
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (viewMode == ProfileViewMode.SELF) {
            Button(
                onClick = onLogout,
                modifier = Modifier.weight(1f).height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("退出登录", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onNavigateToEditProfile?.invoke() },
                modifier = Modifier.weight(1f).height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("编辑资料", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onToggleFollow,
                modifier = Modifier.weight(1f).height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowed) Color(0xFF2E2E2E) else Color(0xFFFE2C55)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    if (isFollowed) "已关注" else "关注",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Button(
                onClick = {},
                modifier = Modifier.weight(1f).height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("私信", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2E2E2E))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}
