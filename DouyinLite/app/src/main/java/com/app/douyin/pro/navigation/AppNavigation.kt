package com.app.douyin.pro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.douyin.pro.feature.auth.ui.LoginScreen
import com.app.douyin.pro.feature.auth.ui.RegisterScreen
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
            ProfileScreen(
                onNavigateToLogin = { navController.navigate(NavRoutes.LOGIN) }
            )
        }
        composable(NavRoutes.MALL) {
            MallScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) },
                onLoginSuccess = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(NavRoutes.LOGIN) },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }
    }
}

