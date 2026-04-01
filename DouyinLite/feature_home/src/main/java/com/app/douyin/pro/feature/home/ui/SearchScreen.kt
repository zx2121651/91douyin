package com.app.douyin.pro.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

val BgColor = Color(0xFF11131E)
val SurfaceContainerLowest = Color(0xFF0B0E18)
val SurfaceContainerLow = Color(0xFF191B26)
val SurfaceContainer = Color(0xFF1D1F2A)
val SurfaceContainerHigh = Color(0xFF272935)
val SurfaceContainerHighest = Color(0xFF323440)
val Primary = Color(0xFFFFB3B6)
val OnPrimary = Color(0xFF680019)
val PrimaryContainer = Color(0xFFFF5168)
val ErrorContainer = Color(0xFF93000A)
val OnSurface = Color(0xFFE1E1F1)
val OnSurfaceVariant = Color(0xFFE6BCBD)
val SecondaryContainer = Color(0xFF05ECE7)
val OnSecondaryContainer = Color(0xFF006764)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .statusBarsPadding()
    ) {
        // Top Navigation Bar & Search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(SurfaceContainerHighest, CircleShape)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "北京冬奥会 精彩瞬间",
                    color = OnSurface,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.PlayArrow, // placeholder for camera
                    contentDescription = "Camera",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "取消",
                color = OnSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onCancel() }
            )
        }

        // Category Tabs
        val tabs = listOf("综合", "视频", "用户", "直播", "商品", "话题")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(tabs.size) { index ->
                val isSelected = index == 0
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = tabs[index],
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(Primary, CircleShape)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Hot Discussion Topic Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(ErrorContainer, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("热议：北京冬奥会两周年回顾", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(4f/3f).clip(RoundedCornerShape(8.dp))) {
                            AsyncImage(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuB18Oga7IBKcVWtLty1A0y3_1n3jerfS4Dcm8UZ1f9nJEVTjT6KiGVvv55_2Kbd_jJhKRrRhu3ZgFtBrL1AwljtzEssUiPnXeTQ8hZvaIUNiY_hmtKy28JPYC34Sa5gI1EsyqggqWyW1G7digCxWDn-ODZ13DuAZujCwiVBnPJk5b8pzpfqdQxd-G8Tvs9r_vkL5JTq-tkfJpxIpBwx17pZGviPcTsAO4l15nlUkXJeiZA2Ge3D_jc1wJGpRmfQO3MIYpv37Il1Hg4",
                                contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                            )
                            Row(
                                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)
                                    .background(Color.Black.copy(alpha=0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription=null, tint=Color.White, modifier=Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("1.2亿", color = Color.White, fontSize = 10.sp)
                            }
                        }
                        Box(modifier = Modifier.weight(1f).aspectRatio(4f/3f).clip(RoundedCornerShape(8.dp))) {
                            AsyncImage(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBUdcS5QCRv3JmBxqxNlgeNVy6S-HblaEupJ1fOkSLGaa5WOVVtr5EGGkGE0UilmUUwfbJvs_UtWo12DeCk2gsTeTPDmKd3vhqlFzb7M8cB-acZPqwWXjg3UXNAp5XgCdj6a1o9KGLV2myatUdmGF0k6Netx3BqJXhl-W_QR3uwxzxSCrJeM3nxJcqVRTr55izb0K_Spqvby1JtpPal0GSX3iqYGOwJfWKwWSZMWotQDlPl-yh8jYtHrAE2YkzdIoKYTZSgl7PEcrQ",
                                contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                            )
                            Row(
                                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)
                                    .background(Color.Black.copy(alpha=0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription=null, tint=Color.White, modifier=Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("8903万", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Related Users
            item {
                Text(
                    text = "相关用户",
                    color = OnSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // User 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCrRWxvlVRJyCoVV56-rpj1QKV-8L142oayb6w-C8W0dqsvJc-h3JogiT00LKzjmqc6QCiKdc3s8dnGt5t-PAwsZl4m9wXxh7FYXly5hGSoexlI3Vn8Zvg_0u2qySwaoIcWLRLmJ2EEbYHc46YIYXTJRo0mGplSj49o4GmOXTg_5fSbf23o2N-qmlfqfPePGjPOR8vDCEgeI-zNSQfXuBTZVnSTP1rEROuFkg3NA3Se1VkyhjgDmx61FNI8sJOZZu1c110PY-Md4uQ",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(48.dp).clip(CircleShape).border(2.dp, Primary.copy(alpha=0.2f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("冰雪大玩家", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.CheckCircle, contentDescription=null, tint=Color(0xFF00DDD8), modifier=Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("粉丝: 240.5万 · 视频: 1.2k", color = OnSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                    Box(
                        modifier = Modifier.background(Primary, CircleShape).padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text("关注", color = OnPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // User 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuA3OfeTJLaWheiyj-93ii5552Rd7DTvDMJvj1-59ap3T8bkHx65dWZ-S9FNs953XIO3A_X2ZHs_HQXAsVjj2msai37okBR5nLWThEUak_dfAW8IB27Ui-L25pkmCDlzVk3bnrny7IHyn5fDb7GwB6ustn3IixZJQOS4FVaauxR2BtButrEdWlAbKv4Hq3sp9kSfuk-L01fmLOVjxAEOqiNhn0T0BtvNsnYphHsPcC5OwyzlUXHo3SdP5qF-n2LeTYqzWNrpWDo7Zk8",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("滑雪小周", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("粉丝: 45.2万 · 获赞: 304.1万", color = OnSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                    Box(
                        modifier = Modifier.background(SurfaceContainerHighest, CircleShape).padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text("回访", color = OnSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Mixed Feed Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Column
                    Column(modifier = Modifier.weight(1f)) {
                        // Video Card 1
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(9f/16f).clip(RoundedCornerShape(12.dp)).background(SurfaceContainer)) {
                                AsyncImage(
                                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBJlG4htu3z8e5X6XjnzEAoFmH9rYlX1Ba2ibwQzlDLgRoRahdx4WZMHcdeI33vLO03ZqCFsa7gH7QQ3PtLO9LGawztFuRAuLwk9QgEIkWMPAczmhibQaljsgSQQK5_7iwjV5D37eXZx157M405yh71B2WQkBRDOXtA_tfdN0gMWTWWU7IvH6wFo0JFcmHZeb-GhqNxftxybQi88gKH_cN0TLHJCR_T_2OHBDav5pgvdIvinBwBI3jH8c7SSQMJWoUCtTVHb5re7u0",
                                    contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha=0.6f)), startY = 300f
                                        ))
                                )
                                Row(
                                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Favorite, contentDescription=null, tint=Color.White, modifier=Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("12.4w", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "这就是顶级滑雪选手的视角吗？太刺激了！#冬奥会 #滑雪",
                                color = Color.White, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuB_P_mbz0wUJe4enIwpOBRrxwieBdlOdMu54y9MFNJ1oR_TkXCHnQLC0FsXTDZi3gp7miBpnJ-zJdkZkYnKwIPN2AlMpks1MK_rNzp64N-HoV1DYS1PxH7bsBShlrqHWicSiFGMwoyxrn1H3X_xhGplFeAEUK2NsChwe0arpc1Y3Zl0b_D3sXIA3EE14v7NLdxgkE2q5XULdC4qVxmWH8MU-oCwTt6MjyIi5TquQTplv9M986w7xsniLT48aUa-AnLN9UT9oi3qGlk",
                                    contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(16.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("极速先锋", color = OnSurfaceVariant, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Video Card 2
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(9f/16f).clip(RoundedCornerShape(12.dp)).background(SurfaceContainer)) {
                                AsyncImage(
                                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBXYjHeXxcjENDi_BY4HfwQVYK3O09j7nwwqYep7RM98A2g5z8Bxr9NpFpSQqgZTXtGf_lX-Ap2K2ziwxi8T7SxhvA_at2wSC-IOSHnfFfjN7dVaDvcb02iHb6IHrShzklZ6j66QPrETfXcAS1QkaeMeL4sQWHEUotdy0xCPYTqHeQC3q2AjUhcJyLl0tGcq83y4Hg-c9iJVmY3S_LOkHtgdpUI4-G9MygCg5nEdOnVGmRpXiloTRRMPM1tgT9fimuCM9HiztlN07s",
                                    contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha=0.6f)), startY = 300f
                                        ))
                                )
                                Row(
                                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Favorite, contentDescription=null, tint=Color.White, modifier=Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("8.9w", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "零下30度的浪漫，这冰雕也太美了！",
                                color = Color.White, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDpTJ9gDKFkQkMyH7xPtWYyRI0H0r5ieGSKQ0zQZICnn1CxY3zoeRRCJILvYpgK1N0QSPXOvqjJtBJksmoG-zmKxTgiuE0qalnn5t85_Ajjptp8A6N6VDdA9ZVh7Ca6v2AkD0A5LLz6K-FcfmJw8XyvP_Bqon4JIO6FPDjEYjqtDuj9teEW47urJ1rkADvEXcqi7iTxls-5EwbPlj7iyBqEL9slF5LiKYlMMsZHZbLAp2iaWcWV3xsPjVerfwcV70ZmdqmSsKoChAM",
                                    contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(16.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("旅行摄影家", color = OnSurfaceVariant, fontSize = 10.sp)
                            }
                        }
                    }

                    // Right Column
                    Column(modifier = Modifier.weight(1f)) {
                        // E-commerce Card
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceContainerLow).border(1.dp, Color.White.copy(alpha=0.05f), RoundedCornerShape(12.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                                AsyncImage(
                                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCkQLoEsujMNJMDwEbmXx9SZX1Myt9020i9IusrkNNqAp33gKZjd4k8sij-OJnSLixiNJ6lkhFrlpBTAoWQEvjGMvRCtW69eLloG2eU47ji84vSSs8apokvjqD1q5wZ-AmvimRM5TRRlIrqkwpuNLlFS9rKiX3wgWxPo6IpEbBKzzpiIUMppYM8YazP3HiVSz8PRrqMnEltURsx7SoERNNofv5OJCK0p9AIwFvsHWReHqbXTDdJkLNBxMD38bWAscX_yCpJzOqQ0B8",
                                    contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                                        .background(SecondaryContainer.copy(alpha=0.9f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("商城推荐", color = OnSecondaryContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    "专业防雾滑雪镜 大球面护目镜 官方同款",
                                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("¥299", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("¥599", color = OnSurfaceVariant, fontSize = 10.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.border(1.dp, Primary.copy(alpha=0.2f), RoundedCornerShape(4.dp)).background(Primary.copy(alpha=0.1f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("双11低价", color = Primary, fontSize = 8.sp)
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("1.2万人已买", color = OnSurfaceVariant, fontSize = 9.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Small Topic Card
                        Column(
                            modifier = Modifier.fillMaxWidth().background(SurfaceContainerHigh, RoundedCornerShape(12.dp)).padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Primary.copy(alpha=0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#", color = Primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("#我和冬奥有个约会", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("4.5亿次播放", color = OnSurfaceVariant, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFF323440), CircleShape).padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("立即参与", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
