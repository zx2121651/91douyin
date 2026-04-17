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
import com.app.douyin.pro.feature.home.ui.SearchScreen
import com.app.douyin.pro.feature.inbox.ui.ChatScreen
import com.app.douyin.pro.feature.inbox.ui.InboxScreen
import com.app.douyin.pro.feature.mall.ui.MallScreen
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
                onNext = { exportedUri ->
                    navController.navigate(NavRoutes.buildPublishRoute(exportedUri.toString()))
                }
            )
        }
        composable(
            route = NavRoutes.PUBLISH,
            arguments = listOf(navArgument("videoUri") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val videoUri = backStackEntry.arguments?.getString("videoUri") ?: ""
            PublishScreen(
                videoUri = videoUri,
                onBack = { navController.popBackStack() },
                onPublishSuccess = {
                    navController.navigate(NavRoutes.ME) {
                        popUpTo(NavRoutes.HOME) { inclusive = false }
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
        composable(
            route = NavRoutes.USER_PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) {
            ProfileScreen(
                onNavigateToLogin = { navController.navigate(NavRoutes.LOGIN) },
                onBack = { navController.popBackStack() }
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

