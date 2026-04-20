package com.app.douyin.pro.navigation

object NavRoutes {
    const val HOME = "home"
    const val FRIENDS = "friends"
    const val RECORD = "record"
    const val INBOX = "inbox"
    const val NOTIFICATIONS = "notifications"
    const val ME = "me"
    const val EDIT_PROFILE = "edit_profile"

    const val EDIT = "edit?videoUri={videoUri}"
    fun buildEditRoute(videoUri: String): String {
        return "edit?videoUri=${android.net.Uri.encode(videoUri)}"
    }

    const val CHAT = "chat/{userId}/{userName}"
    fun buildChatRoute(userId: Long, userName: String): String {
        return "chat/$userId/${android.net.Uri.encode(userName)}"
    }

    const val MALL = "mall_standalone"

    const val PUBLISH = "publish?videoUri={videoUri}"
    fun buildPublishRoute(videoUri: String): String {
        return "publish?videoUri=${android.net.Uri.encode(videoUri)}"
    }

    const val SEARCH = "search"
    const val SEARCH_PLAYER = "search_player?keyword={keyword}&index={index}"
    fun buildSearchPlayerRoute(keyword: String, index: Int): String {
        return "search_player?keyword=${android.net.Uri.encode(keyword)}&index=$index"
    }

    const val USER_PROFILE = "profile/{userId}"
    fun buildUserProfileRoute(userId: Long): String {
        return "profile/$userId"
    }

    const val LOGIN = "login"
    const val REGISTER = "register"
}
