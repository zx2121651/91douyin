@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.app.douyin.pro.feature.home.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.douyin.pro.feature.home.ui.components.*
import com.app.douyin.pro.feature.home.ui.components.CommentsBottomSheet
import com.app.douyin.pro.feature.home.viewmodel.HomeViewModel
import com.app.douyin.pro.feature.home.viewmodel.LoadState
import com.app.douyin.pro.lib.media.model.PagingState
import com.app.douyin.pro.lib.media.model.VideoModel
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToMall: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val horizontalPagerState = rememberPagerState(initialPage = 3, pageCount = { 4 })
    val selectedTopTabIndex = horizontalPagerState.currentPage + 1

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (val loadState = uiState.loadState) {
            is LoadState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF2C55))
                }
            }
            is LoadState.Error -> {
                ErrorView(
                    message = loadState.message,
                    errorCode = loadState.error?.code,
                    onRetry = { viewModel.loadInitialData() }
                )
            }
            is LoadState.Success -> {
                if (loadState.isEmpty) {
                    EmptyView(onRetry = { viewModel.loadInitialData() })
                } else {
                    HomeContent(
                        uiState = uiState,
                        horizontalPagerState = horizontalPagerState,
                        coroutineScope = coroutineScope,
                        onNavigateToMall = onNavigateToMall,
                        onNavigateToProfile = onNavigateToProfile,
                    onSearchClick = onNavigateToSearch,
                        onLoadMore = { viewModel.loadMore() }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun HomeContent(
    uiState: com.app.douyin.pro.feature.home.viewmodel.HomeUiState,
    horizontalPagerState: androidx.compose.foundation.pager.PagerState,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onNavigateToMall: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSearchClick: () -> Unit,
    onLoadMore: () -> Unit
) {
    val selectedTopTabIndex = horizontalPagerState.currentPage + 1
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> LiveScreen()
                1 -> { /* Place for Local Screen */ }
                2 -> VideoFeed(
                    videos = uiState.videos.reversed(),
                    isVisible = horizontalPagerState.currentPage == 2,
                    onNavigateToProfile = onNavigateToProfile,
                    pagingState = uiState.pagingState,
                    onLoadMore = onLoadMore
                )
                3 -> VideoFeed(
                    videos = uiState.videos,
                    isVisible = horizontalPagerState.currentPage == 3,
                    onNavigateToProfile = onNavigateToProfile,
                    pagingState = uiState.pagingState,
                    onLoadMore = onLoadMore
                )
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
                    coroutineScope.launch { horizontalPagerState.animateScrollToPage(index) }
                },
                onSearchClick = onSearchClick,
                onNavigateToMall = onNavigateToMall,
                modifier = Modifier.statusBarsPadding()
            )
        }
    }
}

@Composable
fun ErrorView(message: String, errorCode: Int? = null, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Warning,
            contentDescription = null,
            tint = Color(0xFFFF2C55),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "加载失败",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (errorCode != null) "$message ($errorCode)" else message,
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.width(140.dp).height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2C55))
        ) {
            Text("点击重试", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyView(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Inbox,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无视频内容",
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.width(120.dp),
            border = BorderStroke(1.dp, Color.Gray),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
            Text("重试")
        }
    }
}

@Composable
fun VideoPage(video: VideoModel, isVisible: Boolean) {
    val viewModel: HomeViewModel = hiltViewModel()
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
                    onTap = { isPaused = !isPaused },
                    onDoubleTap = {
                        if (!video.isLiked) viewModel.toggleLike(video.id)
                    },
                    onLongPress = { showLongPressMenu = true }
                )
            }
    ) {
        VideoPlayerComponent(url = video.playUrl, coverUrl = video.coverUrl, isVisible = isVisible, isDucked = showCommentsSheet, isPaused = isPaused)

        ActionPanel(
            isLiked = video.isLiked,
            likeCount = video.likeCount,
            commentCount = video.commentCount,
            shareCount = video.shareCount,
            avatarUrl = video.author.avatar,
            isFollowed = video.author.isFollowed,
            onLikeClick = { viewModel.toggleLike(video.id) },
            onFollowClick = { viewModel.toggleFollow(video.id) },
            onCommentClick = { showCommentsSheet = true },
            onShareClick = { showShareSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        )

        VideoOverlay(
            author = video.author.name,
            description = video.title,
            musicTitle = "原声 - 潮流音乐库"
        )

        if (showCommentsSheet) {
            val commentViewModel: com.app.douyin.pro.feature.home.viewmodel.CommentViewModel = hiltViewModel()
            LaunchedEffect(video.id) {
                commentViewModel.loadComments(video.id, video.commentCount)
            }
            CommentsBottomSheet(
                videoId = video.id,
                commentCount = video.commentCount,
                onDismiss = { showCommentsSheet = false },
                viewModel = commentViewModel
            )
        }
        if (showShareSheet) ShareBottomSheet(onDismiss = { showShareSheet = false })
        if (showLongPressMenu) LongPressMenu(onDismiss = { showLongPressMenu = false })
    }
}

@Composable
fun TopNavigationBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToMall: () -> Unit,
    modifier: Modifier
) {
    val tabs = listOf("商城", "直播", "城市", "关注", "推荐")
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Menu, null, tint = Color.White, modifier = Modifier.size(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTabIndex
                Text(
                    text = title,
                    color = Color.White.copy(alpha = if (isSelected) 1f else 0.7f),
                    fontSize = if (isSelected) 18.sp else 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.clickable { if (index == 0) onNavigateToMall() else onTabSelected(index - 1) }
                )
            }
        }
        Icon(Icons.Filled.Search, null, tint = Color.White, modifier = Modifier.size(28.dp).clickable { onSearchClick() })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color(0xFF161823)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("分享到", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                val shares = listOf("微信", "朋友圈", "QQ", "复制链接")
                items(shares) { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Share, null, tint = Color.White)
                        }
                        Text(item, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun LongPressMenu(onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.width(280.dp).background(Color(0xFF2E2E2E), RoundedCornerShape(16.dp)).padding(8.dp)) {
            listOf("不感兴趣", "保存视频", "收藏", "举报").forEach {
                Text(it, color = Color.White, modifier = Modifier.fillMaxWidth().clickable { onDismiss() }.padding(16.dp), textAlign = TextAlign.Center)
            }
        }
    }
}