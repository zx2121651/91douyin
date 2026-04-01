package com.app.douyin.pro.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun LiveScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Background Video Stream (Mockup)
        AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCnll6tFQk--dcak5XYUU3ZJGgVvZzxNw4PDlfjkCzNrCxDcvmLxGjoouf8DbGYpQ-qipkMYF7oKxFQeRJmdDEOsxRq0tBjWAnXaSPN3bwhCNXT63_jwpFddliHXA0MlpxCc3p2z87dKCr4MKuVUqB-vEfyCjqG3SyoqAi6evFeR0sCOH8KNrhlsCPFC_kBExhp9GZP5t5AZealVUcGn0RZ7pWlikQZPhKz15_7KneEEKO53hQcunp7d-HlnQ4J5qaUenzzk_W2P_I",
            contentDescription = "Live Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.8f
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        LiveTopOverlay(modifier = Modifier.align(Alignment.TopCenter))

        LiveLeftArea(modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 80.dp, start = 16.dp))
        LiveRightArea(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 120.dp, end = 16.dp))

        LiveBottomBar(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun LiveBottomBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text Input Placeholder
        Row(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .background(Color(0xFF191B26).copy(alpha = 0.6f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("说点什么...", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
        }

        // Icons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Cart with badge
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF191B26).copy(alpha = 0.6f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFB3B6), CircleShape)
                        .border(2.dp, Color.Black, CircleShape)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("99+", color = Color(0xFF680019), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Gift
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFB3B6).copy(alpha = 0.3f), Color(0xFFFF5168))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Star, contentDescription = "Gift", tint = Color.White)
            }

            // Share
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF191B26).copy(alpha = 0.6f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
            }

            // More
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF191B26).copy(alpha = 0.6f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More", tint = Color.White)
            }
        }
    }
}

@Composable
fun LiveLeftArea(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(260.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Badges
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .background(Color(0xFF93000A).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFFFB3B6).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).background(Color(0xFFFFB3B6), CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
            Row(
                modifier = Modifier
                    .background(Color(0xFF191B26).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = Color(0xFF35FBF5), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("直播间专属优惠中", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Comments
        Box(
            modifier = Modifier
                .height(200.dp)
                // Gradient mask for fading out at the top
                // We'll approximate the mask with a vertical gradient box over it or just plain for now
                .background(Color.Transparent)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CommentItem("小丸子", "主播真漂亮，衣服也好看。", Color(0xFF35FBF5))
                }
                item {
                    CommentItem("系统提示", "欢迎来到直播间，领券购买更划算！", Color(0xFFFFB3B6), isSystem = true)
                }
                item {
                    CommentItem("科技迷", "刚刚下单了，期待发货！🚀", Color(0xFF35FBF5))
                }
                item {
                    CommentItem("李华", "这个款式还有现货吗？主播快介绍一下！", Color(0xFF35FBF5))
                }
            }
        }

        // Product Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuA1o8-meF2FMOI7BYkT-UeZkorYvqbULbCUaes7bykcW0J9XcjfeRm6CT7y1c-iXm2orIun1iRBN-wRuHAn28jkqXDbTDKEnW5ihpRlYaMZUyESg7zdArapPzdlWzZHjNhtKy9jRP9cbJs3pN7Wa_JMx6ul67F1S0A2ypByRer7BwtKegxLQ2-BN3sMAzDVrIkntzK56Bv64Shny16nqYOENNP2G1CaZatSUmAsZTJRcke8uBMCFAsrMv09eAgghd2yOlmvoWiwU1E",
                contentDescription = "Product",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "限定版科技联名系列 极简美学",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("¥299.00", color = Color(0xFFFF5168), fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text("¥499.00", color = Color.Gray, fontSize = 9.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFFF5168), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(author: String, content: String, authorColor: Color, isSystem: Boolean = false) {
    val bgColor = if (isSystem) Color(0xFFFFB3B6).copy(alpha = 0.2f) else Color(0xFF191B26).copy(alpha = 0.6f)
    val borderColor = if (isSystem) Color(0xFFFFB3B6).copy(alpha = 0.3f) else Color.Transparent

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = "$author: ", color = authorColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = content, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun LiveRightArea(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Floating Heart (Mockup)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Like",
                tint = Color(0xFFFFB3B6),
                modifier = Modifier.size(36.dp)
            )
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Like Small",
                tint = Color(0xFFFFB3B6).copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp).padding(top = 8.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun LiveTopOverlay(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Profile Info Box
            Row(
                modifier = Modifier
                    .background(Color(0xFF191B26).copy(alpha = 0.6f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .padding(end = 12.dp, top = 4.dp, bottom = 4.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCawHKDD5Bc-t3mdNm73zZSVkNZ8zlFyKslN5lz0PH_Ygxf4Xnn2PRmcjezeF3L7nNGLAZ9JUJv27oBlZ_1QyHkxlBtB1Avg266S3GKeqCTj2R94NYfX9wHeWa1xdWqNLgC3ThkXQx07hQQu5yNlj_tY4rt8uyZd4nN7qTAtdXte0TtD5g1rSIaa7vX1aWE8d2V5uJkng8DddsZ4X2gTxoltVIxsgM_9IFPzlivjd-bkeT1Snw6kvl0qESqOG2PjApVSBNjjKO0dj4",
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFFFB3B6), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("主播林悦", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("1.2w 观看", color = Color(0xFFFFB3B6), fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Follow Button
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFB3B6), Color(0xFFFF5168))
                            ),
                            shape = CircleShape
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("关注", color = Color(0xFF680019), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Ranking / Top Givers
            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                val avatars = listOf(
                    "https://lh3.googleusercontent.com/aida-public/AB6AXuC_vWDnSRDui3WgkMFLjiLCBfefaJVwLHrgYoTwaarOSqUz8XYDVqbL9lVHRbImx27Brz0sgfzNvbznC-MpckwQAWaXKr0N7dLwMCZ8zCjmKNm5RS4H4GanAZek5R5MngZfa49XvTFFWqBkwuVXk89E5vuKrPoxvFjROfR9v7WblrCPVhKSfBqdsHH75cZ2kTlI7CJeCMv_hDBfhEbkiIl8rjZREJ2wGo3s1J8kXbFZqE68Zo3Pd5ttFQ5IFO_GLLwjHkFjiznuyvk",
                    "https://lh3.googleusercontent.com/aida-public/AB6AXuADAA54trJS2jC2p6UpKheycWoywHncQfXc5qdMzYDFWp6iyIfvbkBpqJyCO2p1KuKKGrsfCPuRXUUP8JtwnJ6zjDEmI7TIppBL9dLvweB2GVVPAtYAgb6LFOIw90QWs970XgrYEUjqJ6oW_gfvNaSHz41G2XugTtEwyHADI1qv1OvgYpdpobPNGyGSrsvcfXiBnbAct5CTYUV71j1MbqFdb9FZi_g3IKhdjLAqyvbv8W5oqpFjqZvPc7771EWPm-CZAVh1fnB8gmk",
                    "https://lh3.googleusercontent.com/aida-public/AB6AXuDLmp8Ys8jvi7huDY52vEtRqa8PFls9IOplQo1GITSeRGRz9bnOfix-VDINlWOpZAXtPhPRARXielx7Bmov6cbxWav6WCw68XhlDHyjfyjAaQzQmNeydpfNY5XZ99JcdPpRU44p1lB59ycDP2-vXh_wZKnVCzrCRImFavKifPgb9V5ebtZbMA1OakI83zMeX5IXK3QZClcVoNsfenJVrnDi1YNtHdDp6P8nCCAatrI-A0tDNyGda8tm-mdCKyrW4cpb1O4vM7XFLAs"
                )
                avatars.forEach { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Viewer",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFF35FBF5), CircleShape)
                    )
                }
            }
        }

        // Close Icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF191B26).copy(alpha = 0.6f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}
