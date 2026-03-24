package com.app.douyin.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.douyin.pro.feature.home.ui.HomeScreen
import com.app.douyin.pro.feature.record.ui.RecordScreen
import com.app.douyin.pro.feature.profile.ui.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup edge-to-edge content for immersive mode
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            DouyinLiteApp(navController)
        }
    }
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "首页", Icons.Filled.Home)
    object Friends : BottomNavItem("friends", "朋友", Icons.Filled.Search) // Using Search as placeholder
    object Record : BottomNavItem("record", "拍摄", Icons.Filled.AddCircle)
    object Messages : BottomNavItem("messages", "消息", Icons.Filled.MailOutline)
    object Me : BottomNavItem("me", "我", Icons.Filled.Person)
}

@Composable
fun DouyinLiteApp(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Friends,
        BottomNavItem.Record,
        BottomNavItem.Messages,
        BottomNavItem.Me
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Optional: Hide bottom bar on specific screens like Record
            if (currentRoute != BottomNavItem.Record.route) {
                NavigationBar(
                    containerColor = if (currentRoute == BottomNavItem.Home.route) Color.Black else Color.White,
                    contentColor = if (currentRoute == BottomNavItem.Home.route) Color.White else Color.Black
                ) {
                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(text = item.title) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (currentRoute == BottomNavItem.Home.route) Color.White else Color.Black,
                                unselectedIconColor = Color.Gray,
                                selectedTextColor = if (currentRoute == BottomNavItem.Home.route) Color.White else Color.Black,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                navController.navigate(item.route) {
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
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen()
            }
            composable(BottomNavItem.Friends.route) {
                // Placeholder
                Box(modifier = Modifier.fillMaxSize()) { Text("Friends Screen") }
            }
            composable(BottomNavItem.Record.route) {
                RecordScreen()
            }
            composable(BottomNavItem.Messages.route) {
                // Placeholder
                Box(modifier = Modifier.fillMaxSize()) { Text("Messages Screen") }
            }
            composable(BottomNavItem.Me.route) {
                ProfileScreen()
            }
        }
    }
}
