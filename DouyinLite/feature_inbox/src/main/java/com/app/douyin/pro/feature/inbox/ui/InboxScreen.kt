package com.app.douyin.pro.feature.inbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.douyin.pro.feature.inbox.domain.model.Conversation
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory
import com.app.douyin.pro.feature.inbox.ui.vm.InboxUiState
import com.app.douyin.pro.feature.inbox.ui.vm.InboxViewModel

val DarkSurface = Color(0xFF161823)
val DarkSurfaceContainer = Color(0xFF252632)
val TextPrimary = Color(0xFFE1E1F1)
val TextSecondary = Color(0xFF909191)
val ErrorColorDot = Color(0xFFFF0050)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun InboxScreen(
    onNavigateToChat: (Long, String) -> Unit = { _, _ -> },
    onNavigateToNotifications: () -> Unit = {},
    viewModel: InboxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // For pull refresh, we only show indicator if we are Success but refreshing
    // Actually let's use a separate refresh state if we wanted to be perfect,
    // but we can use the current state.
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState is InboxUiState.Loading,
        onRefresh = { viewModel.refresh() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("消息", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 48.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { }, modifier = Modifier.padding(end = 8.dp).size(36.dp).clip(CircleShape).background(DarkSurfaceContainer)) {
                        Icon(Icons.Default.Search, "Search", tint = TextPrimary, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkSurface
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).pullRefresh(pullRefreshState)) {
            when (val state = uiState) {
                is InboxUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF0050))
                    }
                }
                is InboxUiState.Empty -> {
                    EmptyInboxView { viewModel.refresh() }
                }
                is InboxUiState.Error -> {
                    ErrorInboxView(state.message) { viewModel.refresh() }
                }
                is InboxUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item { NotificationCategoriesRow(state.categories, onNavigateToNotifications) }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(state.conversations, key = { it.id }) { conversation ->
                            ConversationItemRow(conversation, onNavigateToChat)
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState is InboxUiState.Loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = DarkSurfaceContainer,
                contentColor = Color(0xFFFF0050)
            )
        }
    }
}

@Composable
fun EmptyInboxView(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.MailOutline, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("暂无消息", color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0050))
        ) {
            Text("刷新一下")
        }
    }
}

@Composable
fun ErrorInboxView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("加载失败", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, color = TextSecondary, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = onRetry,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF0050)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF0050))
        ) {
            Text("重新加载")
        }
    }
}

@Composable
fun NotificationCategoriesRow(
    categories: List<NotificationCategory>,
    onNavigateToNotifications: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        categories.forEach { category ->
            CategoryItem(category, onNavigateToNotifications)
        }
    }
}

@Composable
fun CategoryItem(
    category: NotificationCategory,
    onNavigateToNotifications: () -> Unit
) {
    val iconColor = when(category.type) {
        "group" -> Color(0xFFE2E2E2)
        "favorite" -> Color(0xFFFFB3B6)
        "alternate_email" -> Color(0xFF35FBF5)
        else -> Color(0xFFC6C6C7)
    }

    val icon = when(category.type) {
        "group" -> Icons.Default.Person
        "favorite" -> Icons.Default.Favorite
        "alternate_email" -> Icons.Default.Email
        else -> Icons.Default.Email
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            if (category.type == "favorite" || category.type == "alternate_email") {
                onNavigateToNotifications()
            }
        }
    ) {
        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(DarkSurfaceContainer), contentAlignment = Alignment.Center) {
            Icon(icon, category.title, tint = iconColor, modifier = Modifier.size(28.dp))
            if (category.hasDot) {
                Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp).size(10.dp).clip(CircleShape).background(ErrorColorDot))
            }
            category.badgeText?.let {
                Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-6).dp).clip(RoundedCornerShape(10.dp)).background(ErrorColorDot).padding(horizontal = 4.dp, vertical = 1.dp)) {
                    Text(it, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(category.title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ConversationItemRow(conversation: Conversation, onNavigateToChat: (Long, String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable {
            val targetId = conversation.id.toLongOrNull() ?: 1L
            onNavigateToChat(targetId, conversation.name)
        }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp)) {
            AsyncImage(model = conversation.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape).then(if (conversation.isLive) Modifier.background(Color(0xFFFF5168), CircleShape).padding(2.dp).clip(CircleShape) else Modifier))
            if (conversation.isOfficial) {
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(14.dp).clip(CircleShape).background(Color(0xFF35FBF5)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF00504D), modifier = Modifier.size(10.dp))
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(conversation.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(conversation.lastTime, color = TextSecondary, fontSize = 12.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(conversation.lastMessage, color = TextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (conversation.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .defaultMinSize(minWidth = 16.dp)
                            .height(16.dp)
                            .clip(CircleShape)
                            .background(ErrorColorDot)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
