package com.app.douyin.pro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.app.douyin.pro.feature.home.ui.SearchScreen
import com.app.douyin.pro.feature.home.ui.SearchPlayerScreen
import com.app.douyin.pro.feature.inbox.ui.ChatScreen
import com.app.douyin.pro.feature.inbox.ui.InboxScreen
import com.app.douyin.pro.feature.inbox.ui.NotificationCenterScreen
import com.app.douyin.pro.feature.mall.ui.MallScreen
import com.app.douyin.pro.feature.profile.ui.ProfileEditScreen
import com.app.douyin.pro.feature.profile.ui.ProfileScreen
import com.app.douyin.pro.feature.record.ui.PublishScreen
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
                onNavigateToProfile = { navController.navigate(NavRoutes.ME) },
                onNavigateToSearch = { navController.navigate(NavRoutes.SEARCH) }
            )
        }
        composable(NavRoutes.FRIENDS) {
            FriendsScreen()
        }
        composable(NavRoutes.RECORD) {
            RecordScreen(onNavigateToEdit = { segmentsJson ->
                navController.navigate(NavRoutes.buildEditMultiRoute(segmentsJson))
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
                onNext = { exportedUri, coverMs ->
                    navController.navigate(NavRoutes.buildPublishRoute(exportedUri.toString(), coverMs))
                }
            )
        }
        composable(
            route = NavRoutes.EDIT_MULTI,
            arguments = listOf(navArgument("segments") { type = NavType.StringType })
        ) { backStackEntry ->
            val segmentsJson = backStackEntry.arguments?.getString("segments") ?: "[]"
            EditScreen(
                segmentsJson = segmentsJson,
                onClose = { navController.popBackStack() },
                onNext = { exportedUri, coverMs ->
                    navController.navigate(NavRoutes.buildPublishRoute(exportedUri.toString(), coverMs))
                }
            )
        }
        composable(
            route = NavRoutes.PUBLISH,
            arguments = listOf(
                navArgument("videoUri") { type = NavType.StringType; defaultValue = "" },
                navArgument("coverTimestamp") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val videoUri = backStackEntry.arguments?.getString("videoUri") ?: ""
            val coverMs = backStackEntry.arguments?.getLong("coverTimestamp") ?: 0L
            PublishScreen(
                videoUri = videoUri,
                coverTimestamp = coverMs,
                onBack = { navController.popBackStack() },
                onPublishSuccess = {
                    navController.navigate(NavRoutes.ME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
                    }
                }
            )
        }
        composable(NavRoutes.INBOX) {
            InboxScreen(
                onNavigateToChat = { userId, userName ->
                    navController.navigate(NavRoutes.buildChatRoute(userId, userName))
                },
                onNavigateToNotifications = {
                    navController.navigate(NavRoutes.NOTIFICATIONS)
                }
            )
        }
        composable(NavRoutes.NOTIFICATIONS) {
            NotificationCenterScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = NavRoutes.CHAT,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ChatScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.ME) {
            ProfileScreen(
                onNavigateToLogin = { navController.navigate(NavRoutes.LOGIN) },
                onNavigateToEditProfile = { navController.navigate(NavRoutes.EDIT_PROFILE) },
                onVideoClick = { index ->
                    // For simplicity, we use a placeholder keyword for personal profile video consumption
                    navController.navigate(NavRoutes.buildSearchPlayerRoute("__me__", index))
                }
            )
        }
        composable(NavRoutes.EDIT_PROFILE) {
            ProfileEditScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = NavRoutes.USER_PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            ProfileScreen(
                onNavigateToLogin = { navController.navigate(NavRoutes.LOGIN) },
                onBack = { navController.popBackStack() },
                onVideoClick = { index ->
                    navController.navigate(NavRoutes.buildSearchPlayerRoute("__user_${userId}__", index))
                }
            )
        }
        composable(NavRoutes.MALL) {
            MallScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onVideoClick = { keyword, index ->
                    navController.navigate(NavRoutes.buildSearchPlayerRoute(keyword, index))
                },
                onUserClick = { userId ->
                    navController.navigate(NavRoutes.buildUserProfileRoute(userId))
                }
            )
        }
        composable(
            route = NavRoutes.SEARCH_PLAYER,
            arguments = listOf(
                navArgument("keyword") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val keyword = backStackEntry.arguments?.getString("keyword") ?: ""
            val index = backStackEntry.arguments?.getInt("index") ?: 0

            // Get the ViewModel from the Search Screen's backstack entry to share it
            val searchBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(NavRoutes.SEARCH)
            }
            val searchViewModel: com.app.douyin.pro.feature.home.viewmodel.SearchViewModel = hiltViewModel(searchBackStackEntry)

            SearchPlayerScreen(
                keyword = keyword,
                initialIndex = index,
                onBack = { navController.popBackStack() },
                viewModel = searchViewModel
            )
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

