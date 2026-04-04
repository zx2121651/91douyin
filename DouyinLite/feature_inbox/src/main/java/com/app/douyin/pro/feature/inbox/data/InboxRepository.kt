package com.app.douyin.pro.feature.inbox.data

import com.app.douyin.pro.feature.inbox.domain.model.Message
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxRepository @Inject constructor() {
    fun getMessages(): List<Message> {
        return listOf(
            Message(
                id = "1",
                avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBsb9heggAmdGakJhZpKSPjZnujTYoF7iEn8LXu29mvlyPpjREH_cPzUPLl00zDBWgw41po0vyXWPfqzqceK4uFectOiA4BtN0IF2mqKXUVtTpzmJN7_8JIaK1mY_gkYFnxnkFYpncLQ-ZXuu9__Ps7ZA6uJkKnNIpHrc_2-vZkfYgcRbfAFyu6JQqtO9b8d_bgMRvHbkBNv291JfsrHysOudS6XxK7TSQuFbKE37rUZg40zMx09GoK7skcIuHzsfaJ1lnOAbmIsYI",
                name = "抖音小助手",
                time = "昨天",
                content = "你的视频已被推荐到首页，快去看看吧！",
                isOfficial = true,
                hasUnreadDot = true
            ),
            Message(
                id = "2",
                avatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD8I_U9yAByHrNztp31d3S5Ql5HDcVsXOtOffLNhtuX4qaajnkwFgdAFL5OCuwdLzNBs9QDqqeiJejfbJPzXVeArU5eX10395R9he1IM-Eoy2kh6lmFA_v6n8auwbHfT6iBKAZdZODWoz0wWWJn57dDE7AybZhChYpQ6vVgt7ESF1A6VaNFSrjxMK6MuHftCkoxICASpEx6ooT2VDLv3mlsVbLQNXGa1uCeoOWCamXI699HkQHUvmOk",
                name = "编程小哥",
                time = "2小时前",
                content = "大佬，最新的 Compose 教程什么时候出？",
                isLive = true
            )
        )
    }

    fun getCategories(): List<NotificationCategory> {
        return listOf(
            NotificationCategory("group", "粉丝", hasDot = true),
            NotificationCategory("favorite", "赞", hasDot = true),
            NotificationCategory("alternate_email", "@我的"),
            NotificationCategory("chat_bubble", "评论", badgeText = "9+")
        )
    }
}
