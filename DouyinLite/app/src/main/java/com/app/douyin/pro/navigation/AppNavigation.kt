package com.app.douyin.pro.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.douyin.pro.feature.edit.ui.EditScreen
import com.app.douyin.pro.feature.home.ui.FriendsScreen
import com.app.douyin.pro.feature.home.ui.HomeScreen
import com.app.douyin.pro.feature.inbox.ui.ChatScreen
import com.app.douyin.pro.feature.inbox.ui.InboxScreen
import com.app.douyin.pro.feature.mall.ui.MallScreen
import com.app.douyin.pro.feature.profile.ui.ProfileScreen
import com.app.douyin.pro.feature.record.ui.RecordScreen

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToMall = { navController.navigate(NavRoutes.MALL) },
                onNavigateToProfile = { navController.navigate(NavRoutes.ME) }
            )
        }
        composable(NavRoutes.FRIENDS) {
            FriendsScreen()
        }
        composable(NavRoutes.RECORD) {
            RecordScreen(onNavigateToEdit = { videoUriStr ->
                navController.navigate(NavRoutes.buildEditRoute(videoUriStr))
            })
        }
        composable(
            route = NavRoutes.EDIT,
            arguments = listOf(navArgument("videoUri") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val videoUri = backStackEntry.arguments?.getString("videoUri") ?: ""
            EditScreen(
                videoUri = videoUri,
                onClose = { navController.popBackStack() },
                onNext = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.INBOX) {
            InboxScreen(onNavigateToChat = { userId, userName ->
                navController.navigate(NavRoutes.buildChatRoute(userId, userName))
            })
        }
        composable(
            route = NavRoutes.CHAT,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
            ChatScreen(
                userId = userId,
                userName = userName,
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.ME) {
            ProfileScreen()
        }
        composable(NavRoutes.MALL) {
            MallScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val isDarkBgRoute = NavigationConfigs.isDarkBackgroundRoute(currentRoute)

    NavigationBar(
        containerColor = if (isDarkBgRoute) Color.Transparent else Color.White,
        contentColor = if (isDarkBgRoute) Color.White else Color.Black
    ) {
        MainTab.items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    if (item == MainTab.Record) {
                        RecordIcon()
                    } else {
                        item.icon?.let { Icon(it, contentDescription = item.title, modifier = Modifier.padding(bottom = 2.dp)) }
                    }
                },
                label = {
                    if (item != MainTab.Record) {
                        Text(
                            text = item.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.sp
                        )
                    }
                },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (isDarkBgRoute) Color.White else Color.Black,
                    unselectedIconColor = Color.LightGray,
                    selectedTextColor = if (isDarkBgRoute) Color.White else Color.Black,
                    unselectedTextColor = Color.LightGray,
                    indicatorColor = Color.Transparent
                ),
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

@Composable
private fun RecordIcon() {
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .drawBehind {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF00E5FF), Color(0xFFFF0050))
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                )
                drawRoundRect(
                    color = Color.White,
                    size = androidx.compose.ui.geometry.Size(size.width - 8.dp.toPx(), size.height),
                    topLeft = androidx.compose.ui.geometry.Offset(4.dp.toPx(), 0f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Add, contentDescription = "拍摄", tint = Color.Black)
    }
}
