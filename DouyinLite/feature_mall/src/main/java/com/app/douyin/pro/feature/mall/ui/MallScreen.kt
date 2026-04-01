package com.app.douyin.pro.feature.mall.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DarkSurface = Color(0xFF11131E)
val LightSurface = Color(0xFF1D1F2A)
val SurfaceVariant = Color(0xFF323440)
val OutlineVariant = Color(0xFF5D3F40)

@Composable
fun MallScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            PromotionalBanner()
            CategoriesGrid()
            LiveStreamingSection()
            ProductGridSection()
        }

        // Sticky Top App Bar
        TopAppBar()
    }
}

@Composable
fun TopAppBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                )
            )
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x99323440)) // backdrop-blur approximation
                    .border(1.dp, OutlineVariant.copy(alpha = 0.2f), CircleShape)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "iPhone 15 Pro Max 直降...",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Cart",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun PromotionalBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(21f / 9f)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
    ) {
        // Background Image using Coil
        coil.compose.AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCTPiUm53Sg0GI1dw_axqS-WdIrhQPMcAmrt3ooLqmVbkWTYc4jM7TaVW9X4NTnxw8yDF5c9WzUlU6V0CyMTCpRsOkr3-kupRFrrs1ShwbO56StCogU3BoarwUCJwk8RYD0lY8g8weZn1ipE-VkRYXHF_2skrMevZ8PqjrUq1-FFN5juN4ua3hLYED_RB99jiai9lgLxA_yJf1eIGWgfEC4Rbrqj6TqIihMqD9WNuKBnO7GlG_uwZ7oCl50RbnA6BFHHqIOStYWFWA",
            contentDescription = "Promotional Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFff5168).copy(alpha = 0.8f), Color.Transparent)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Double 11 Tag
                Box(
                    modifier = Modifier
                        .background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "DOUBLE 11",
                        color = Color(0xFF5B0015),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = "年度大促\n全场五折起",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "11月11日 00:00 准时开抢",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CategoriesGrid() {
    val categories = listOf(
        Triple("美妆", Icons.Default.Face, Color(0xFFffb3b6)),
        Triple("数码", Icons.Default.Devices, Color(0xFF35fbf5)),
        Triple("美食", Icons.Default.Restaurant, Color(0xFFffdada)),
        Triple("服饰", Icons.Default.Checkroom, Color(0xFFaffffb)),
        Triple("更多", Icons.Default.GridView, Color.White)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        categories.forEach { (name, icon, color) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .background(Color(0xFF272935)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = name,
                    color = Color(0xFFe6bcbd),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LiveStreamingSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFffb3b6))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "正在直播",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
            Text(
                text = "查看全部",
                color = Color(0xFFe6bcbd),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal List
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LiveStreamingCard(
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuATykphx2bGurYu4Mk-W2kIvizSXMbtZW6Y5q2GAb3rqcAfNpHmu2Nh2jEcPN2i_ofdRgZmqs6RmY2mC_oaqNUcrP77dgLZF5AQj96T8w-nnJjM11jvoyVlns-cILYDiuLx-R8QO69xyYPon35251qH1xOm3swMOCx2CeGWl6zOMGeywEV_nVGVh9Fm2hRboKqK3aG60vJAxh3Av0WzkTYUo1u9hwy4zGnyG7Zb4oATUZpvAFgffqym0URu-k357j4YMjXcEf1cfU0",
                    viewCount = "12.5k",
                    title = "李佳琦双11爆款预告"
                )
            }
            item {
                LiveStreamingCard(
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCUrQ8wxyNFUN9pZDGY1zRngcwk_hjjUXIvhrZAWHLffJv3AQ0VUtQ-ZGXO9thMYWM1HYRKH6n90nqevRiuS0iNDTJubdAhfR-hUCVWVMVRypYpbUgLw91vI7iTwWgM69nDAXLKdvttxZMPCvVy28ehIjmZbuG7IaCM2UHHLKpqHEXGqQC_BcvnW4TQoKa8Kp6jWAdUrjFFFdPveokj19rc-iLizjGLLzk81bWRCcty5-gCpBV3NxuagvAV8vuQ_gRPXlm6s0FiH9M",
                    viewCount = "8.1k",
                    title = "高端数码新品首发"
                )
            }
        }
    }
}

@Composable
fun LiveStreamingCard(imageUrl: String, viewCount: String, title: String) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .aspectRatio(9f / 16f)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
    ) {
        coil.compose.AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // LIVE Badge
        Row(
            modifier = Modifier
                .padding(8.dp)
                .background(Color(0xFF93000a), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "LIVE",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black
            )
        }

        // View Count Badge
        Text(
            text = "$viewCount 观看",
            color = Color.White,
            fontSize = 8.sp,
            modifier = Modifier
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.4f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .align(Alignment.TopEnd)
        )

        // Title Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 100f // starts fade a bit below top
                    )
                )
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun ProductGridSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Tabs
        val tabs = listOf("猜你喜欢", "百亿补贴", "精选特惠", "今日上新")
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tabs.size) { index ->
                val title = tabs[index]
                val isSelected = index == 0
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        color = if (isSelected) Color(0xFFffb3b6) else Color(0xFFe6bcbd),
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(Color(0xFFffb3b6))
                        )
                    }
                }
            }
        }

        // Grid (simulated with Columns inside Row since lazy grid doesn't easily grow inside scrollable col)
        // For simplicity in scrollable Column, we use simple Rows
        val products = listOf(
            ProductItem(
                "Nike Air Max 270 运动鞋 男子减震跑鞋官方正品",
                "899",
                "已售 2.4万+",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDOHDxUTkmeMKZDNzNhsxB3XtbN5pCd5NxwXypmJvN8ZRME_kEfvzwLamuxFVD32YY94ILQR3FBWcHn5kgiUNxNc9h6JY_vg4sj4TCiuC5P7pC32t4vmrZ_fNhGjHADZplISkIRUxvE0PnEcODyJ1jFBq-xVfbgIR-Sv-BktaQ64Kbg0lIfPafJVgh8Lw7XU3tmCRsBR1ftMgs1p_TAz2z1gvf64qktQKTBarE9ebTQCVi1aTw6k8TbcCSAk0YPTXT5VKagKMXx_Xc",
                "低价好物",
                Color(0xFFff5168)
            ),
            ProductItem(
                "极简主义设计师系列 蓝宝石镜面防水石英表",
                "1599",
                "已售 1.1万+",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuCwcMe4y_g7Gzirn0P0ueZnRuL-UfjuWQrtTifAdKQaJBBMWXlvq9FnArtEw031YNJKyy8mYTQcT8ybXhyiDTvUx4nYe7fmy12jZnfLjQaXutJCdpgbEK4klXROR96zc-o_4ocLHiIL2vJNrqrDmNmoIpw8hOvdKg2E8sM5aDEAYwJ0Wal8ZzkhpT1ucAphmG9TGshV6usasNls6CLFHtjq3Bn1SO7thJf3bVgXcAt-EvSosnN6GsjLGgejrWUMBfF65qzJbRvEeHE",
                "品牌闪购",
                Color(0xFF00ddd8),
                Color(0xFF00201f)
            ),
            ProductItem(
                "RTX 4090 旗舰电竞主机 性能怪兽级体验",
                "21999",
                "月销 500+",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDIhdMHp1ipaWSk5mmWNM2BuyVIhfUTGNvj5amjgiY_RU469144_AEfwb6eXdWlGqSSOUkoSLrRLQuQMNKenHGXMhEPFuj3B3gRU7HFctEqdkVIW_LEHXis_zw-3s9iyDvNag3JysaEJ_H_QqW_xvjAJtxhHK3RL3vgA4fWe8ALQuXdXgDr8p_RiC0sK9hOQn-6e5Qd_Yl5XM3ssPDvi0HgkdwaWYH2qRLo93updOvg5pZ21vLTfXlXT9NymkWacgBaLQBngPtvqjM",
                "",
                Color.Transparent
            ),
            ProductItem(
                "CHANEL 香奈儿 五号香水 经典持久留香 50ml",
                "1050",
                "已售 3万+",
                "https://lh3.googleusercontent.com/aida-public/AB6AXuARmoeiyLkaPsZK5GKa1-yMy0Yz0PxRCsWeLgnihJqzCv47jZ2eCI9soPZ8HTkdNA2_yQI1hULln41es05-WNMmqRZqDMcK92P8imjMQDkcUZNbNHZ1MKr5gRtCjMR0kSFfhh9-6wxrh3pCLaG2Y4vlLOuwWnVGiVE5TsWHVfiFOdGvCGb_wK4d7nT_t7b20nhvZOjxye_ouqtCc9B3ekJ5jrm4dSVUsHQVr1zfvqJRUop0plrQ5OJ3EOsbzsfUroHG-1iee_abJJ8",
                "",
                Color.Transparent
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ProductCard(products[0])
                Spacer(modifier = Modifier.height(16.dp))
                ProductCard(products[2])
                Spacer(modifier = Modifier.height(100.dp)) // Bottom padding
            }
            Column(modifier = Modifier.weight(1f)) {
                ProductCard(products[1])
                Spacer(modifier = Modifier.height(16.dp))
                ProductCard(products[3])
                Spacer(modifier = Modifier.height(100.dp)) // Bottom padding
            }
        }
    }
}

data class ProductItem(
    val title: String,
    val price: String,
    val sales: String,
    val imageUrl: String,
    val tag: String,
    val tagBgColor: Color,
    val tagTextColor: Color = Color.White
)

@Composable
fun ProductCard(item: ProductItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .background(Color(0xFF191b26))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Square image for all for simplicity
            ) {
                coil.compose.AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                if (item.tag.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(item.tagBgColor, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.tag,
                            color = item.tagTextColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    modifier = Modifier.height(32.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "¥", color = Color(0xFFff5168), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = item.price, color = Color(0xFFff5168), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.sales,
                        color = Color(0xFFe6bcbd),
                        fontSize = 9.sp
                    )

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFff5168)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Add to cart",
                            tint = Color(0xFF5b0015),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
