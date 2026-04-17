package com.app.douyin.pro.feature.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.douyin.pro.feature.home.ui.components.VideoFeed
import com.app.douyin.pro.feature.home.viewmodel.SearchViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchPlayerScreen(
    keyword: String,
    initialIndex: Int,
    onBack: () -> Unit,
    viewModel: SearchViewModel
) {
    val videoResults by viewModel.videoResults.collectAsState()
    val pagingState by viewModel.pagingState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (videoResults.isNotEmpty()) {
            VideoFeed(
                videos = videoResults,
                isVisible = true,
                onNavigateToProfile = { /* Navigate to profile if needed */ },
                pagingState = pagingState,
                initialPage = initialIndex.coerceIn(0, videoResults.size - 1),
                onLoadMore = { viewModel.loadMore() }
            )
        }

        // Top Bar with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "搜索: $keyword",
                color = Color.White,
                fontSize = 16.sp,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
