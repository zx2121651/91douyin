package com.app.douyin.pro

import android.os.Bundle
import androidx.core.view.WindowCompat

import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.app.douyin.pro.feature.mall.ui.MallScreen

@androidx.compose.foundation.ExperimentalFoundationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup edge-to-edge content for immersive mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()


        // Request Permissions
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var allGranted = true
            permissions.entries.forEach {
                if (!it.value) allGranted = false
            }
            if (!allGranted) {
                Toast.makeText(this, "需要授予必要权限才能正常使用", Toast.LENGTH_SHORT).show()
            }
        }

        val requiredPermissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

        requestPermissionLauncher.launch(requiredPermissions)

        setContent {
            val navController = rememberNavController()
            DouyinLiteApp(navController)
        }
    }
}

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector?) {
    object Home : BottomNavItem("home", "首页", Icons.Filled.Home)
    object Friends : BottomNavItem("friends", "朋友", Icons.Filled.Person)
    object Record : BottomNavItem("record", "拍摄", null)
    object Mall : BottomNavItem("mall", "商城", Icons.Filled.ShoppingCart)
    object Me : BottomNavItem("me", "我", Icons.Filled.Person)
}

@Composable
@androidx.compose.foundation.ExperimentalFoundationApi
fun DouyinLiteApp(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Friends,
        BottomNavItem.Record,
        BottomNavItem.Mall,
        BottomNavItem.Me
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Optional: Hide bottom bar on specific screens like Record
            if (currentRoute != BottomNavItem.Record.route) {
                NavigationBar(
                    containerColor = if (currentRoute == BottomNavItem.Home.route || currentRoute == BottomNavItem.Mall.route) Color.Transparent else Color.White,
                    contentColor = if (currentRoute == BottomNavItem.Home.route || currentRoute == BottomNavItem.Mall.route) Color.White else Color.Black
                ) {
                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            icon = {
                                if (item == BottomNavItem.Record) {
                                    Box(
                                        modifier = Modifier
                                            .width(44.dp)
                                            .height(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .drawBehind {
                                                drawRoundRect(
                                                    brush = Brush.horizontalGradient(
                                                        colors = listOf(
                                                            Color(0xFF00E5FF), // Cyan
                                                            Color(0xFFFF0050)  // Red
                                                        ),
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
                                        Icon(
                                            Icons.Filled.Add,
                                            contentDescription = item.title,
                                            tint = Color.Black
                                        )
                                    }
                                } else {
                                    item.icon?.let { Icon(it, contentDescription = item.title, modifier = Modifier.padding(bottom = 2.dp)) }
                                }
                            },
                            label = {
                                if (item != BottomNavItem.Record) {
                                    Text(
                                        text = item.title,
                                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                        fontSize = 10.sp
                                    )
                                }
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (currentRoute == BottomNavItem.Home.route || currentRoute == BottomNavItem.Mall.route) Color.White else Color.Black,
                                unselectedIconColor = Color.LightGray,
                                selectedTextColor = if (currentRoute == BottomNavItem.Home.route || currentRoute == BottomNavItem.Mall.route) Color.White else Color.Black,
                                unselectedTextColor = Color.LightGray,
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
            // Do not pad the Home screen, Record screen, and Mall screen so they can be full-screen
            modifier = Modifier.padding(
                bottom = if (navController.currentBackStackEntryAsState().value?.destination?.route == BottomNavItem.Home.route ||
                             navController.currentBackStackEntryAsState().value?.destination?.route == BottomNavItem.Record.route ||
                             navController.currentBackStackEntryAsState().value?.destination?.route == BottomNavItem.Mall.route)
                         0.dp else innerPadding.calculateBottomPadding()
            )
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen()
            }
            composable(BottomNavItem.Friends.route) {

                Box(modifier = Modifier.fillMaxSize()) { Text("Friends Screen") }
            }
            composable(BottomNavItem.Record.route) {
                RecordScreen()
            }
            composable(BottomNavItem.Mall.route) {

                MallScreen()
            }
            composable(BottomNavItem.Me.route) {
                ProfileScreen()
            }
        }
    }
}
