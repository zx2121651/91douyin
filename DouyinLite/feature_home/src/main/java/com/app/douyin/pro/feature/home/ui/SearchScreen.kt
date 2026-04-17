package com.app.douyin.pro.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.douyin.pro.feature.home.viewmodel.SearchResultType
import com.app.douyin.pro.feature.home.viewmodel.SearchUiState
import com.app.douyin.pro.feature.home.viewmodel.SearchViewModel
import com.app.douyin.pro.lib.media.model.UserModel
import com.app.douyin.pro.lib.media.model.VideoModel
import com.app.douyin.pro.lib.media.util.CountFormatter

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onVideoClick: (String, Int) -> Unit = { _, _ -> },
    viewModel: SearchViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val videoResults by viewModel.videoResults.collectAsState()
    val userResults by viewModel.userResults.collectAsState()
    val combinedSuggestions by viewModel.combinedSuggestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pagingState by viewModel.pagingState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF161823))
            .statusBarsPadding()
    ) {
        SearchTopBar(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                viewModel.onQueryChanged(it)
            },
            onSearch = { viewModel.search(searchQuery) },
            onBack = onBack
        )

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is SearchUiState.Idle, is SearchUiState.Suggesting -> {
                    UnifiedSearchSuggestions(
                        suggestions = combinedSuggestions,
                        onItemClick = {
                            searchQuery = it
                            viewModel.search(it)
                        },
                        onDeleteHistory = { viewModel.deleteHistory(it) },
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }
                is SearchUiState.Results -> {
                    SearchResultContent(
                        selectedTab = selectedTab,
                        onTabSelect = { viewModel.selectTab(it) },
                        videoResults = videoResults,
                        userResults = userResults,
                        isLoading = isLoading,
                        pagingState = pagingState,
                        onLoadMore = { viewModel.loadMore() },
                        onLikeClick = { viewModel.toggleLike(it) },
                        onFollowClick = { viewModel.toggleFollow(it) },
                        onVideoClick = { index -> onVideoClick(searchQuery, index) }
                    )
                }
                is SearchUiState.Empty -> {
                    EmptySearchView(onRetry = { if (searchQuery.isNotEmpty()) viewModel.search(searchQuery) })
                }
                is SearchUiState.Error -> {
                    ErrorSearchView(message = state.message, onRetry = { if (searchQuery.isNotEmpty()) viewModel.search(searchQuery) })
                }
            }
        }
    }
}

@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .clickable { onBack() }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF2B2C33)),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(Color(0xFFFE2C55)),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text("搜你想看的", color = Color.Gray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                )
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onQueryChange("") }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "搜索",
            color = Color(0xFFFE2C55),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onSearch() }
        )
    }
}

@Composable
fun UnifiedSearchSuggestions(
    suggestions: List<com.app.douyin.pro.feature.home.viewmodel.SearchSuggestion>,
    onItemClick: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    val history = suggestions.filter { it.type == com.app.douyin.pro.feature.home.viewmodel.SuggestionType.HISTORY }
    val hot = suggestions.filter { it.type == com.app.douyin.pro.feature.home.viewmodel.SuggestionType.HOT }
    val suggests = suggestions.filter { it.type == com.app.douyin.pro.feature.home.viewmodel.SuggestionType.SUGGEST }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (suggests.isNotEmpty()) {
            items(suggests) { suggest ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(suggest.content) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = suggest.content,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFF2B2C33),
                    thickness = 0.5.dp
                )
            }
        } else {
            if (history.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("搜索历史", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear All",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onClearHistory() }
                        )
                    }
                }
                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp
                    ) {
                        history.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2B2C33))
                                    .clickable { onItemClick(item.content) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.content, color = Color.LightGray, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable { onDeleteHistory(item.content) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (hot.isNotEmpty()) {
                item {
                    Text(
                        "猜你想搜",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                item {
                    // Use a Box with custom layout or just a Column of Rows to avoid hardcoded height of LazyVerticalGrid
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        val chunks = hot.chunked(2)
                        chunks.forEach { rowItems ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                rowItems.forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f).clickable { onItemClick(item.content) }
                                    ) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = Color(0xFFFE2C55),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            item.content,
                                            color = Color.LightGray,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
        content = { content() }
    )
}

@Composable
fun SearchResultContent(
    selectedTab: SearchResultType,
    onTabSelect: (SearchResultType) -> Unit,
    videoResults: List<VideoModel>,
    userResults: List<UserModel>,
    isLoading: Boolean,
    pagingState: com.app.douyin.pro.feature.home.viewmodel.PagingState,
    onLoadMore: () -> Unit,
    onLikeClick: (Long) -> Unit,
    onFollowClick: (Long) -> Unit,
    onVideoClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (selectedTab == SearchResultType.Video) 0 else 1,
            containerColor = Color.Transparent,
            contentColor = Color(0xFFFE2C55),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == SearchResultType.Video) 0 else 1]),
                    color = Color(0xFFFE2C55)
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTab == SearchResultType.Video,
                onClick = { onTabSelect(SearchResultType.Video) },
                text = { Text("视频", fontSize = 15.sp) }
            )
            Tab(
                selected = selectedTab == SearchResultType.User,
                onClick = { onTabSelect(SearchResultType.User) },
                text = { Text("用户", fontSize = 15.sp) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == SearchResultType.Video) {
                VideoResultList(
                    videos = videoResults,
                    isLoading = isLoading,
                    pagingState = pagingState,
                    onLoadMore = onLoadMore,
                    onLikeClick = onLikeClick,
                    onVideoClick = onVideoClick
                )
            } else {
                UserResultList(
                    users = userResults,
                    isLoading = isLoading,
                    pagingState = pagingState,
                    onLoadMore = onLoadMore,
                    onFollowClick = onFollowClick
                )
            }
        }
    }
}

@Composable
fun VideoResultList(
    videos: List<VideoModel>,
    isLoading: Boolean,
    pagingState: com.app.douyin.pro.feature.home.viewmodel.PagingState,
    onLoadMore: () -> Unit,
    onLikeClick: (Long) -> Unit,
    onVideoClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(videos.size) { index ->
            VideoResultItem(
                video = videos[index],
                onLikeClick = onLikeClick,
                onClick = { onVideoClick(index) }
            )
        }
        item {
            if (isLoading || pagingState is com.app.douyin.pro.feature.home.viewmodel.PagingState.Loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFE2C55), modifier = Modifier.size(24.dp))
                }
            } else if (pagingState is com.app.douyin.pro.feature.home.viewmodel.PagingState.Error) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "加载失败，点击重试",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { onLoadMore() }
                    )
                }
            } else if (videos.isNotEmpty()) {
                LaunchedEffect(Unit) { onLoadMore() }
            }
        }
    }
}

@Composable
fun VideoResultItem(video: VideoModel, onLikeClick: (Long) -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF252632))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = video.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(82.dp)
                .fillMaxHeight()
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = video.author.avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(video.author.name, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Icon(
                    if (video.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (video.isLiked) Color(0xFFFE2C55) else Color.Gray,
                    modifier = Modifier.size(14.dp).clickable { onLikeClick(video.id) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(CountFormatter.format(video.likeCount), color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun UserResultList(
    users: List<UserModel>,
    isLoading: Boolean,
    pagingState: com.app.douyin.pro.feature.home.viewmodel.PagingState,
    onLoadMore: () -> Unit,
    onFollowClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(users) { user ->
            UserResultItem(user = user, onFollowClick = onFollowClick)
        }
        item {
            if (isLoading || pagingState is com.app.douyin.pro.feature.home.viewmodel.PagingState.Loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFE2C55), modifier = Modifier.size(24.dp))
                }
            } else if (pagingState is com.app.douyin.pro.feature.home.viewmodel.PagingState.Error) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "加载失败，点击重试",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { onLoadMore() }
                    )
                }
            } else if (users.isNotEmpty()) {
                LaunchedEffect(Unit) { onLoadMore() }
            }
        }
    }
}

@Composable
fun UserResultItem(user: UserModel, onFollowClick: (Long) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.avatar,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(user.signature ?: "该用户很懒，什么都没有留下", color = Color.Gray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("粉丝: ${CountFormatter.format(user.followerCount)}", color = Color.Gray, fontSize = 12.sp)
        }
        Button(
            onClick = { onFollowClick(user.id) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (user.isFollowed) Color(0xFF2B2C33) else Color(0xFFFE2C55)
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.height(28.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(if (user.isFollowed) "已关注" else "关注", fontSize = 12.sp, color = Color.White)
        }
    }
}

@Composable
fun EmptySearchView(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("未找到相关结果", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE2C55))) {
                Text("重试")
            }
        }
    }
}

@Composable
fun ErrorSearchView(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE2C55))) {
                Text("重试")
            }
        }
    }
}
