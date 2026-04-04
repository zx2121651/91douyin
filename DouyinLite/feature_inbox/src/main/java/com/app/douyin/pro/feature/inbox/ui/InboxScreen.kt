package com.app.douyin.pro.feature.inbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.douyin.pro.feature.inbox.domain.model.Message
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory
import com.app.douyin.pro.feature.inbox.ui.vm.InboxViewModel

val DarkSurface = Color(0xFF161823)
val DarkSurfaceContainer = Color(0xFF252632)
val TextPrimary = Color(0xFFE1E1F1)
val TextSecondary = Color(0xFF909191)
val ErrorColorDot = Color(0xFFFF0050)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: InboxViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val categories by viewModel.categories.collectAsState()

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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item { NotificationCategoriesRow(categories) }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(messages) { message -> MessageItemRow(message) }
        }
    }
}

@Composable
fun NotificationCategoriesRow(categories: List<NotificationCategory>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        categories.forEach { category ->
            CategoryItem(category)
        }
    }
}

@Composable
fun CategoryItem(category: NotificationCategory) {
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

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { }) {
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
fun MessageItemRow(message: Message) {
    Row(modifier = Modifier.fillMaxWidth().clickable { }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp)) {
            AsyncImage(model = message.avatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape).then(if (message.isLive) Modifier.background(Color(0xFFFF5168), CircleShape).padding(2.dp).clip(CircleShape) else Modifier))
            if (message.isOfficial) {
                Box(modifier = Modifier.align(Alignment.BottomEnd).size(14.dp).clip(CircleShape).background(Color(0xFF35FBF5)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF00504D), modifier = Modifier.size(10.dp))
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(message.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(message.time, color = TextSecondary, fontSize = 12.sp)
            }
            Text(message.content, color = TextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
