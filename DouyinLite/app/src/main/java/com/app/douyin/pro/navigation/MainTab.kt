package com.app.douyin.pro.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class MainTab(
    val route: String,
    val title: String,
    val icon: ImageVector?
) {
    data object Home : MainTab(NavRoutes.HOME, "首页", Icons.Filled.Home)
    data object Friends : MainTab(NavRoutes.FRIENDS, "朋友", Icons.Filled.Person)
    data object Record : MainTab(NavRoutes.RECORD, "拍摄", null)
    data object Inbox : MainTab(NavRoutes.INBOX, "消息", Icons.Filled.Email)
    data object Me : MainTab(NavRoutes.ME, "我", Icons.Filled.Person)

    companion object {
        val items = listOf(Home, Friends, Record, Inbox, Me)
    }
}
