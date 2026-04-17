package com.app.douyin.pro.navigation

object NavRoutes {
    const val HOME = "home"
    const val FRIENDS = "friends"
    const val RECORD = "record"
    const val INBOX = "inbox"
    const val ME = "me"

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

    const val LOGIN = "login"
    const val REGISTER = "register"
}
