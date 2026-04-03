@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.app.douyin.pro.feature.home.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.app.douyin.pro.feature.home.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToMall: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val videos = viewModel.videos
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val horizontalPagerState = rememberPagerState(initialPage = 3, pageCount = { 4 })
    val selectedTopTabIndex = horizontalPagerState.currentPage + 1

    var showSearchScreen by remember { mutableStateOf(false) }

    if (showSearchScreen) {
        SearchScreen(onCancel = { showSearchScreen = false })
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> LiveScreen()
                1 -> { /* Place for Local Screen */ }
                2 -> VideoFeed(videos = videos.reversed(), isVisible = horizontalPagerState.currentPage == 2, onNavigateToProfile = onNavigateToProfile)
                3 -> VideoFeed(videos = videos, isVisible = horizontalPagerState.currentPage == 3, onNavigateToProfile = onNavigateToProfile)
            }
        }

        AnimatedVisibility(
            visible = horizontalPagerState.currentPage != 0,
            enter = fadeIn(),
            \x65xit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopNavigationBar(
                selectedTabIndex = selectedTopTabIndex,
                onTabSelected = { index ->
                    coroutineScope.launch { horizontalPagerState.animateScrollToPage(index) }
                },
                onSearchClick = { showSearchScreen = true },
                onNavigateToMall = onNavigateToMall,
                modifier = Modifier.statusBarsPadding()
            )
        }
    }
}

@Composable
fun VideoPage(url: String, isVisible: Boolean) {
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
                    onDoubleTap = { /* Double tap for heart animation could be added here */ },
                    onLongPress = { showLongPressMenu = true }
                )
            }
    ) {
        VideoPlayerComponent(url = url, isVisible = isVisible, isDucked = showCommentsSheet, isPaused = isPaused)

        ActionPanel(
            onCommentClick = { showCommentsSheet = true },
            onShareClick = { showShareSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        )

        VideoOverlay(
            author = "潮流先锋",
            description = "这是一段非常精彩的视频描述 #抖音 #Compose",
            musicTitle = "原声 - 潮流音乐库"
        )

        if (showCommentsSheet) CommentsBottomSheet(onDismiss = { showCommentsSheet = false })
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
fun CommentsBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = Color(0xCC000000)) {
        Column(modifier = Modifier.fillMaxHeight(0.7f).padding(16.dp)) {
            Text("评论 (128)", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.3f))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(20) { Text("精彩评论 #$it", color = Color.White, modifier = Modifier.padding(vertical = 8.dp)) }
            }
        }
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
