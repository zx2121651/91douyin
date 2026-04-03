package com.app.douyin.pro.feature.inbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Add


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

val DarkSurface = Color(0xFF161823)
val DarkSurfaceContainer = Color(0xFF252632)
val TextPrimary = Color(0xFFE1E1F1)
val TextSecondary = Color(0xFF909191)
val PrimaryColor = Color(0xFFFFB3B6)
val ErrorColor = Color(0xFF93000A)
val ErrorColorDot = Color(0xFFFF0050)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "消息",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 48.dp) // Offset for search icon to keep centered
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkSurface
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp) // For bottom nav
        ) {
            item {
                NotificationCategories()
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // System Message
            item {
                MessageItem(
                    avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBsb9heggAmdGakJhZpKSPjZnujTYoF7iEn8LXu29mvlyPpjREH_cPzUPLl00zDBWgw41po0vyXWPfqzqceK4uFectOiA4BtN0IF2mqKXUVtTpzmJN7_8JIaK1mY_gkYFnxnkFYpncLQ-ZXuu9__Ps7ZA6uJkKnNIpHrc_2-vZkfYgcRbfAFyu6JQqtO9b8d_bgMRvHbkBNv291JfsrHysOudS6XxK7TSQuFbKE37rUZg40zMx09GoK7skcIuHzsfaJ1lnOAbmIsYI",
                    name = "抖音小助手",
                    time = "昨天",
                    message = "你的视频已被推荐到首页，快去看看吧！",
                    isVerified = true,
                    isOfficial = true
                )
            }

            // Friend Message 1
            item {
                MessageItem(
                    avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD50J-wrH3-2wFsREubcSZgdjTzrEV-0OO9PuxFbfvYDVJR1t2BL2LStkwMPoUlnf1SnVeRwKjwEsyc7n3kKAX88yMdlRg-PqorvuaaF4njexx1vVKuKLMu4lfJQLFPQN8Q6yX_dyBYo4tKs01vmYksyRmyNQWNUyz6SxyO6ggOkudSnwRt6h1FC86jfVLs60sAgfcE7YLeAcxOWJ7iuTPReToR6JXf9s5HVbl46Mq_f_Qw3Dbj6cRPG2UPxPvOrCoLJ9quegu0Ch4",
                    name = "林静",
                    time = "10:42",
                    message = "[视频] 哈哈哈哈这个太好笑了，你一定要看！",
                    hasUnreadDot = true,
                    isTimeHighlighted = true
                )
            }

            // Friend Message 2
            item {
                MessageItem(
                    avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC0Zrv36ViWn6cy4fbypqqbwguImkz_c4pjL13YCknGf_s_AXleFL_7lsTy5AQNpNGEcdEwBxCS50qAQmwP5-kt0TLiv0d5ihJZj5WYUfQItUkcDjaGC5aJBm_TdJm4W26pMuYQYhD83l97mSwhvI3UPckEHLvgZCZg13bUIwb46SNXWOGjVtsoGV8Id4mSRM76b3c7FEgTWF5qT8RAnELOtoU3MN6ypJNi3t81rMPKtamB-plGkv1YwM43k0UX15X-Niv4RK4ru4k",
                    name = "Alex Wang",
                    time = "星期二",
                    message = "好的，晚点见。"
                )
            }

            // Live Invite
            item {
                MessageItem(
                    avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD5mPydLRvUqHhfNc53vySO-pUyCM9m0LWm_7_rUMg6iCOUtthXyMguY0h890O_B0GZ6UCDNk8VPSwCaq9kkexZqpDcKZt2_VfFMqG7WdmyT2UJFuONXKcy0OVFMWSVmi4y-cmIvrxbop2VzqaFuid46oNNpcMzUjL8okjZi4e-asFWnQUhEmWoxXe5tZ5_dgWjK_vBBumIjPbCj-mLtHGD72HojkUF9CWRbf4Kmw5iDv1ofEvMyMLYQXwlEHGbh7iq6yBTCfcXhgE",
                    name = "时尚达人Miki",
                    time = "刚刚",
                    message = "邀请你加入直播间",
                    isLive = true,
                    hasActionButton = true,
                    actionButtonText = "进入"
                )
            }
        }
    }
}

@Composable
fun NotificationCategories() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CategoryItem(
            iconColor = Color(0xFFE2E2E2), // Icon tint based on text or specific tint
            text = "粉丝",
            hasDot = true,
            iconRes = "group"
        )
        CategoryItem(
            iconColor = Color(0xFFFFB3B6),
            text = "赞",
            hasDot = true,
            iconRes = "favorite"
        )
        CategoryItem(
            iconColor = Color(0xFF35FBF5),
            text = "@我的",
            hasDot = false,
            iconRes = "alternate_email"
        )
        CategoryItem(
            iconColor = Color(0xFFC6C6C7),
            text = "评论",
            badgeText = "9+",
            iconRes = "chat_bubble"
        )
    }
}

@Composable
fun CategoryItem(
    iconColor: Color,
    text: String,
    hasDot: Boolean = false,
    badgeText: String? = null,
    iconRes: String // Using string to map to our custom or default icons for simplicity, in a real app we'd use ImageVector or DrawableRes
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for icons since we don't have the exact Google Material Symbols as drawables here
            // We use simple shapes or text as a fallback if real icons aren't available, but we can use default Icons
            when (iconRes) {
                "group" -> Icon(imageVector = Icons.Default.Person, contentDescription = text, tint = iconColor, modifier = Modifier.size(28.dp))
                "favorite" -> Icon(imageVector = Icons.Default.Favorite, contentDescription = text, tint = iconColor, modifier = Modifier.size(28.dp))
                "alternate_email" -> Icon(imageVector = Icons.Default.Email, contentDescription = text, tint = iconColor, modifier = Modifier.size(28.dp))
                "chat_bubble" -> Icon(imageVector = Icons.Default.Email, contentDescription = text, tint = iconColor, modifier = Modifier.size(28.dp))
                else -> Box(modifier = Modifier.size(28.dp).background(iconColor))
            }

            if (hasDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ErrorColorDot)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(ErrorColorDot) // inner color
                )
            }

            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorColorDot)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = text,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MessageItem(
    avatarUrl: String,
    name: String,
    time: String,
    message: String,
    isVerified: Boolean = false,
    isOfficial: Boolean = false,
    hasUnreadDot: Boolean = false,
    isTimeHighlighted: Boolean = false,
    isLive: Boolean = false,
    hasActionButton: Boolean = false,
    actionButtonText: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Box
        Box(
            modifier = Modifier.size(48.dp)
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .then(
                        if (isLive) Modifier.background(Color(0xFFFF5168), CircleShape).padding(2.dp).clip(CircleShape)
                        else Modifier
                    )
            )

            if (isOfficial) {
                 Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF35FBF5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Verified",
                        tint = Color(0xFF00504D),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            if (hasUnreadDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(ErrorColorDot)
                )
            }

            if (isLive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF5168))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = time,
                    color = if (isTimeHighlighted) PrimaryColor else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isTimeHighlighted) FontWeight.Medium else FontWeight.Normal,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = message,
                color = if (isTimeHighlighted || isLive) TextPrimary else TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (isTimeHighlighted) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Action Button
        if (hasActionButton) {
            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = Color(0xFF5B0015)
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = actionButtonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(name: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkSurface,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceContainer)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("发送消息...", color = TextSecondary, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(androidx.compose.material.icons.Icons.Default.Face, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = null, tint = TextPrimary)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Mock Message
            Row(verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                        .background(DarkSurfaceContainer)
                        .padding(12.dp)
                ) {
                    Text("你好呀！最近有看到我发的视频吗？", color = TextPrimary, fontSize = 15.sp)
                }
            }
        }
    }
}
