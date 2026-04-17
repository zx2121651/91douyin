package com.app.douyin.pro.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.douyin.pro.feature.inbox.ui.vm.InboxViewModel

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val inboxViewModel: InboxViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (!NavigationConfigs.shouldHideBottomBar(currentRoute)) {
                MainBottomBar(
                    currentRoute = currentRoute,
                    unreadManager = inboxViewModel.unreadManager,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        val noBottomPadding = NavigationConfigs.shouldHaveNoBottomPadding(currentRoute)

        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(
                bottom = if (noBottomPadding) 0.dp else innerPadding.calculateBottomPadding()
            )
        )
    }
}
