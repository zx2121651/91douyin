package com.app.douyin.pro.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

val BackgroundColor = Color(0xFF11131E)
val SurfaceContainerHigh = Color(0xFF272935)
val SurfaceContainerHighest = Color(0xFF323440)
val PrimaryContainer = Color(0xFFFF5168)
val OnSurface = Color(0xFFE1E1F1)
val OnSurfaceVariant = Color(0xFFE6BCBD)
val Primary = Color(0xFFFFB3B6)
val ErrorContainer = Color(0xFF93000A)
val SurfaceContainerLow = Color(0xFF191B26)
val SecondaryFixed = Color(0xFF35FBF5)
val Tertiary = Color(0xFFC6C6C7)

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ProfileHeader()
            }

            items(videoWorks) { work ->
                VideoWorkItem(work)
            }
        }

        // Top App Bar
        TopBar()
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                )
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { }) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
        }
        Row {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
        }
    }
}

@Composable
fun ProfileHeader() {
    Column {
        // Banner
        Box(modifier = Modifier.fillMaxWidth().height(192.dp)) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBatPnORRX0WTyFeJaHKY9DA_lpZxOyKFE0pPJJXJ11Rtir2rVleJtB5uFW9seUm_VB5KPp_7yhQhGVV6dM7CabEgd3IjVD5u8eTRB4XhpXp9qGdPryEQ0ylbdZ54MDfp_WyteYGK_ej87O434QwV-mNyKq6iFttQikf1yShXXCtPhA9Qy65bfk9ZPKi9-YD3c7GwcsOLFPpwXfsJigJ2uQMrbUq2BHfnZeNFbMLlfIQ5nzqNLIEJn0OIKFcrbE2YCDsHJ9gjhxMJk",
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, BackgroundColor)
                        )
                    )
            )
        }

        // Profile Info Block
        Column(modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-48).dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Avatar
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuC5gaon8xdPVctLsHeMF81op5B1P2evIQaCKVUP_Fe8mTeppx4jCg-ERRb9f6hUGBQ923m32BCDwrld9FKfMJzUgl15BqT-lRYZf9311tS9l_Wlrz9jRFw3KpLzDQEQ8ovnpt-yeljoIvAG7fWCbErQ53Zii-Xh0cYOwJ04fxTZHX0DOu8Kg4cdjB11AlQbL9z16uS5u_dWtuQvw-Bv3UVduIDYeYTLUE4Du_kiTVEwx7CCSAWpyZcWhfYQ5WyTgYD1UEPLzaNAK1E",
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(4.dp, BackgroundColor, CircleShape)
                            .background(SurfaceContainerHigh)
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = (-4).dp, y = (-4).dp)
                            .size(24.dp)
                            .background(PrimaryContainer, CircleShape)
                            .border(2.dp, BackgroundColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(SurfaceContainerHighest, RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("编辑资料", color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceContainerHighest, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Add Person", tint = OnSurface, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Identity
            Spacer(modifier = Modifier.height(16.dp))
            Text("User_9921", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("抖音号：8829102", fontSize = 14.sp, color = OnSurfaceVariant, modifier = Modifier.padding(top = 4.dp))

            // Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                Badge(icon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = OnSurfaceVariant) }, text = "南京")
                Badge(text = "24岁")
                Badge(text = "水瓶座")
                Badge(text = "🏫 南京大学")
            }

            // Bio
            Text(
                "热爱生活，探索城市 \uD83D\uDCF8\n分享穿搭与数码科技 | 期待遇见有趣的你",
                fontSize = 14.sp,
                color = OnSurface,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Stats
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(top = 24.dp)) {
                StatItem(value = "1.2w", label = "获赞")
                StatItem(value = "342", label = "关注")
                StatItem(value = "8.9k", label = "粉丝")
                StatItem(value = "45", label = "朋友")
            }

            // Services
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 32.dp).fillMaxWidth()) {
                ServiceCard(icon = { Text("💳", fontSize = 24.sp) }, text = "我的钱包", modifier = Modifier.weight(1f))
                ServiceCard(icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Primary) }, text = "我的订单", modifier = Modifier.weight(1f))
                ServiceCard(icon = { Text("📱", fontSize = 24.sp) }, text = "我的小程序", modifier = Modifier.weight(1f), hasNotification = true)
            }

            // Content Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .background(BackgroundColor.copy(alpha = 0.8f))
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabItem(title = "作品", count = "128", isSelected = true)
                TabItem(title = "私密", count = "12", isSelected = false)
                TabItem(title = "喜欢", count = "1.4w", isSelected = false)
            }
        }
    }
}

@Composable
fun Badge(icon: @Composable (() -> Unit)? = null, text: String) {
    Row(
        modifier = Modifier
            .background(SurfaceContainerHigh, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon?.invoke()
        Text(text, fontSize = 10.sp, color = OnSurfaceVariant)
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 12.sp, color = OnSurfaceVariant)
    }
}

@Composable
fun ServiceCard(icon: @Composable () -> Unit, text: String, modifier: Modifier = Modifier, hasNotification: Boolean = false) {
    Box(
        modifier = modifier
            .background(SurfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            icon()
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        if (hasNotification) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(8.dp)
                    .background(ErrorContainer, CircleShape)
            )
        }
    }
}

@Composable
fun TabItem(title: String, count: String, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = if (isSelected) 0.dp else 2.dp)
    ) {
        Text(title, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else OnSurfaceVariant)
        Text(count, fontSize = 10.sp, color = OnSurfaceVariant.copy(alpha = 0.6f))
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.width(32.dp).height(2.dp).background(Primary))
        }
    }
}

@Composable
fun VideoWorkItem(work: VideoWork) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
    ) {
        AsyncImage(
            model = work.imageUrl,
            contentDescription = "Video Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(12.dp))
            Text(work.views, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (work.isLive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(ErrorContainer, RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text("LIVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (work.isPinned) {
            Box(modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                Text("\uD83D\uDCCC", fontSize = 14.sp)
            }
        }
    }
}

data class VideoWork(val imageUrl: String, val views: String, val isLive: Boolean = false, val isPinned: Boolean = false)

val videoWorks = listOf(
    VideoWork("https://lh3.googleusercontent.com/aida-public/AB6AXuD--KCopxsQcSGhOAVcDcps3YpNvCU3KQy1YhLfIe12pdcEM1HgqjQVV8D2v10sLzBV_C4w9Umtg91uPc3_JnvNddbyYJj9LlYFYxlrIbCrPy6jLnycTY5pyIQkd5RanfJdd6DZfhph5Md2W2UyH5TquGmTjj7-CwZkPztEFDdqmO9wVYapjWOnPoj8IxTAHvhYfXvLPZI0xPIiFcYiwsprMgzGmjN52caw8cvsFnbXOVlcbGgjrNcX5-1NauEWh1dLQeiniMYOZDU", "3.4w", isLive = true),
    VideoWork("https://lh3.googleusercontent.com/aida-public/AB6AXuBUGoS2wCYvK9ew8nLQ80abBrcAYJTfUdAJ_gS6zkH6fLsaJUe6S7pWjlzE6naWYepJ584bnVopO2XQTyDx0dLje0-NnXCaShHu5upaOqYKQehxJRHhxXdEs1W3ENq1ReIAECAxtzfE1QFqPmA8xdf7ydxliGW8wbYWZda0r6I4RQrmqiAg2csgz4CX1MVOu8OBXfIGkjsv8JI53Lysf-pyKfiBDq2IU80ZrSrct7ykg6CiE1GAAtRvJWRYQfY57gfD27jEvgV_BZ4", "1.2w", isPinned = true),
    VideoWork("https://lh3.googleusercontent.com/aida-public/AB6AXuAGqzyWiu7pzu0_SPO_EBl4VaoVxvaccAKqd8BxtgeKY2w5Cu2b5kyIYBa6iXugrhmn6K7-TRJVFkwPGjNwu-VSv1VlrUZZITxtiegfRyTKCSTuH-zFL8FNei8sUjFfQKfLrTqDDhfilCzi_TkmtgQHJuRE8UO_QHKDkaUm7OM5roBzlc-9dNrUKvv_vzBVLxoV_i4w5SgkxFd6fRRrHfn0x4GaASBG5HQWoUpRLJae1CeeOul2uyilCb2mRTnP30wp4tkoBwoLDBg", "8521"),
    VideoWork("https://lh3.googleusercontent.com/aida-public/AB6AXuBS9G3vBagX8oS4slotm5Iw7pSu4HznYCgquH_2l1xmsoq7jl2nWa20VV-spg6BHcPYCC3dXu81Kn8bYHLZpt0lrfPR-0eMA6j1IEI58lwb5JFjhUaBnwty6BCCKG4u4q-zyWa0spmmb2c2mpPSz1Y0F8m5kWjqERygeAw97cYOcFBGZh2v60koF1VtPkKnuiHPHYzwNDzJOD3h5D-TezcIro0jc1osU6AL8BFJt59UHwpVA25YX0VSVJNe2Tsxpefi84SdyEIW2zg", "5562"),
    VideoWork("https://lh3.googleusercontent.com/aida-public/AB6AXuAxztx5LsjRdwWkHi1PDHeObak2vHZq7J4prkG2uYHg9nGVUkCnGvwQ2_Ym8DYbd7QU9cxgpWX2lTfQuhiI91V6r-sc3EsWqBjenol9Sooa7M2r9XQD1s8V79zqPSMLpE1J1EbhsHnsJS0ZwbVqYqozzC0mr9_7sFefcl3F7FV4N3Vvdpee0D3CREX1C3Bz6dn6RI7bXP846H4e3cQhmwuYl6WqpuRkQoL73kPBKXdgATiIrxEOViecITm8gtoxAALKWFzqNSE8pAg", "2.1w"),
    VideoWork("https://lh3.googleusercontent.com/aida-public/AB6AXuApiB_73c3nOz1KN9Phh2CXqf6_9jqj5fCCSGiItSAwEcOwaOFNArC7Udr3_74S8Y9il9gOQ4a6dWllQQmcYcTyJ43OslTRnq8NV7_a5QZrEVdbm14lXr4BSKVahAzt6tqTDMqPPQGRiq0XIBrH-rDDNwkULmJYW4Lp1UNizlYrHA9jEpvqHrbxeE0Onp3x4i7ua9odgkPqVqSjuwSII2f2FY69ygJfLHUZHqWq2yyxd7OvohH8AWjaiCn07PXd0Al86igo6hYC_24", "9931")
)
