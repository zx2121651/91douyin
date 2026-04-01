package com.app.douyin.pro.feature.home.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.douyin.pro.lib.media.VideoPlayerManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendsScreen() {
    val initialVideos = remember { MockData.videos }
    val videos = remember { mutableStateListOf<String>().apply { addAll(initialVideos) } }
    var isLoading by remember { mutableStateOf(false) }
    var pageCount by remember { mutableIntStateOf(1) }

    // Using BeyondBoundsPageCount = 1 to pre-load adjacent pages for smoother scrolling
    // The total page count is the number of videos + 1 (for the DiscoverFriendsSection at the top)
    val pagerState = rememberPagerState(pageCount = { videos.size + 1 })

    // Pagination logic
    LaunchedEffect(pagerState.currentPage) {
        // Load more when reaching the 2nd to last item
        // Subtract 1 from the total size (videos.size + 1) to get the max index,
        // but since page 0 is the Discover screen, the max video index is videos.size.
        if (pagerState.currentPage >= videos.size - 1 && !isLoading) {
            isLoading = true
            val newVideos = MockData.loadMoreVideos(pageCount)
            videos.addAll(newVideos)
            pageCount++
            isLoading = false
        }
    }

    // Observe pager changes for preloading
    val context = LocalContext.current
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
             val playerManager = VideoPlayerManager.getInstance(context)
             // Preload next video
             if (page > 0 && page < videos.size) { // Because page 0 is discover section, video indices are page - 1
                 playerManager.preLoad(videos[page]) // Current page is `page`, so next video is at index `page`
             }
             // Preload previous video
             if (page > 1) {
                 playerManager.preLoad(videos[page - 2]) // Current page is `page`, so previous video is at index `page - 2`
             }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            beyondBoundsPageCount = 1,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page == 0) {
                DiscoverFriendsSection()
            } else {
                val videoIndex = page - 1
                val isVisible = pagerState.currentPage == page
                VideoPage(
                    url = videos[videoIndex],
                    isVisible = isVisible
                )
            }
        }

        FriendsTopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )
    }
}

@Composable
fun FriendsTopBar(
    modifier: Modifier = Modifier
) {
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
            // "关注"
            Text(
                text = "关注",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            // "朋友" (Active)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "朋友",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .background(Color.White, RoundedCornerShape(1.5.dp))
                )
            }

            // "推荐"
            Text(
                text = "推荐",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun DiscoverFriendsSection() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E18)) // surface-container-lowest
    ) {
        // Top Padding for Status Bar + Top App Bar
        Spacer(modifier = Modifier.height(100.dp))

        // Title Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "发现好友",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "你的通讯录好友也在这里",
                    color = Color(0xFFE6BCBD), // on-surface-variant approx
                    fontSize = 14.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "查看全部",
                    color = Color(0xFF35FBF5), // secondary-fixed
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "查看全部",
                    tint = Color(0xFF35FBF5),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Horizontal Recommended Users List
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
        ) {
            item {
                RecommendedUserCard(
                    name = "林小熙_Lacy",
                    subtitle = "可能认识的人",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDwg7CUyKviQKP419naVEyWSaT6yVq91uxH5PASd3I0pKcdieFYQlkPvKqpXXm8_vvwJkTJEU5D_4Cl7ays47F9TQVd83z_uS7qe2-UXRapn1Db4lj0N4NjlN747NUEmMpGpPwnPPwhaiItrvYvez-XU8w5BZtpJmb7J7J4_TdlkGkjcRrVgsMOSo14tArlMNRUbGtEJgdqCbb1INJBmrChnxoM2kfi0HIlNYF_Ao1Hhd1F4b3CCeb25fZHHizVjS5RGivgHlSd0Nk",
                    buttonText = "回关"
                )
            }
            item {
                RecommendedUserCard(
                    name = "张一楠 摄影",
                    subtitle = "来自通讯录好友",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCWIjc544kAejuYDxOrTJE3LucK8ezpR-BrGFiSAcBny80M8ogjBg0SctmCteem4t_U74V840RtZI3PB7-Ktr4EePQKCxf88w594hhB6GX8WVpDboM93NQTDSQfc7Smf3KzL1aaOEFCPp4DgiOykaJj7-dQ0y59QLV46CZ4EEmRP2rCN58x2-P85NBjMNOJmNt1Ox8Xc7VHHW-mi0aXwGOj56rpxna0n8C85p-U3s2gtDrimTFFToe5HagJl3z-mD73GhhkI_XtMTY",
                    buttonText = "关注"
                )
            }
            item {
                RecommendedUserCard(
                    name = "Miya甜点屋",
                    subtitle = "共同好友 5个",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCFhEkFb9fc2Fu0BqnmWKZ7COhjdFa-zOP3KZWzupK9i7lT-bNEPZZiwMh8ieVGqm3pJcfPZPsgHvsAZuACB-appKD9K4iTFBBjKf41cim5IiZiQLWz1V282l-ndokJHZUABcFO5aWx6-nBiERkQcMFBeYgHasIYcWiAF2s4eCqewQVjY2mZu_10XxXXqIyWmXSGjxvBPjrZ8vi5d5yCQm9HRtLuUYsRl9RrcIivyKsougNtqiEBFnUG4QmiyT7-spgGJol81m6WtY",
                    buttonText = "关注"
                )
            }
            item {
                RecommendedUserCard(
                    name = "王明",
                    subtitle = "可能认识的人",
                    imageUrl = "",
                    buttonText = "关注"
                )
            }
        }

        // Feed Teaser at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1D1F2A)), // surface-container
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF323440), CircleShape), // surface-container-highest
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "People",
                        tint = Color(0xFFFFB3B6), // primary
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "你朋友们的动态都在这里",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "下滑查看更多精彩内容",
                    color = Color(0xFFE6BCBD), // on-surface-variant
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Simple Bouncing Arrow Animation (using standard rotation and offset, or just static icon for now)
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Scroll Down",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun RecommendedUserCard(
    name: String,
    subtitle: String,
    imageUrl: String,
    buttonText: String
) {
    Column(
        modifier = Modifier
            .width(144.dp)
            .background(Color(0xFF191B26), RoundedCornerShape(12.dp)) // surface-container-low
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFFFB3B6).copy(alpha = 0.2f), CircleShape) // primary/20
            )

            // Add Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .background(Color(0xFFFFB3B6), CircleShape) // primary
                    .border(2.dp, Color(0xFF191B26), CircleShape), // surface-container-low
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = Color(0xFF680019), // on-primary
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Text(
            text = name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            color = Color(0xFFE6BCBD), // on-surface-variant
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF5168), // primary-container
                contentColor = Color(0xFF5B0015) // on-primary-container
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
