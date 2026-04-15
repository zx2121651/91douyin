package com.app.douyin.pro.navigation

object NavigationConfigs {
    /**
     * Rules to hide the bottom bar for specific routes.
     */
    fun shouldHideBottomBar(route: String?): Boolean {
        return when {
            route == null -> false
            route == NavRoutes.RECORD -> true
            route.startsWith("edit") -> true
            route.startsWith("chat") -> true
            route == NavRoutes.MALL -> true
            else -> false
        }
    }

    /**
     * Rules to use dark background styling for the bottom bar.
     */
    fun isDarkBackgroundRoute(route: String?): Boolean {
        return route == NavRoutes.HOME || route == NavRoutes.FRIENDS
    }

    /**
     * Rules to apply no bottom padding for the content.
     */
    fun shouldHaveNoBottomPadding(route: String?): Boolean {
        return when {
            route == null -> false
            route == NavRoutes.HOME -> true
            route == NavRoutes.FRIENDS -> true
            route == NavRoutes.RECORD -> true
            route.startsWith("edit") -> true
            route.startsWith("chat") -> true
            else -> false
        }
    }
}
