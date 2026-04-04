package com.app.douyin.pro.feature.inbox.data.source

import com.app.douyin.pro.feature.inbox.domain.model.Message
import com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory

interface InboxDataSource {
    fun getMessages(): List<Message>
    fun getCategories(): List<NotificationCategory>
}

class MockInboxDataSource : InboxDataSource {
    override fun getMessages() = listOf(
        Message("1", "https://lh3.googleusercontent.com/aida-public/AB6AXuBsb9heggAmdGakJhZpKSPjZnujTYoF7iEn8LXu29mvlyPpjREH_cPzUPLl00zDBWgw41po0vyXWPfqzqceK4uFectOiA4BtN0IF2mqKXUVtTpzmJN7_8JIaK1mY_gkYFnxnkFYpncLQ-ZXuu9__Ps7ZA6uJkKnNIpHrc_2-vZkfYgcRbfAFyu6JQqtO9b8d_bgMRvHbkBNv291JfsrHysOudS6XxK7TSQuFbKE37rUZg40zMx09GoK7skcIuHzsfaJ1lnOAbmIsYI", "抖音小助手", "昨天", "你的视频已被推荐到首页！", true, true),
        Message("2", "https://lh3.googleusercontent.com/aida-public/AB6AXuD8I_U9yAByHrNztp31d3S5Ql5HDcVsXOtOffLNhtuX4qaajnkwFgdAFL5OCuwdLzNBs9QDqqeiJejfbJPzXVeArU5eX10395R9he1IM-Eoy2kh6lmFA_v6n8auwbHfT6iBKAZdZODWoz0wWWJn57dDE7AybZhChYpQ6vVgt7ESF1A6VaNFSrjxMK6MuHftCkoxICASpEx6ooT2VDLv3mlsVbLQNXGa1uCeoOWCamXI699HkQHUvmOk", "编程小哥", "2小时前", "Compose 教程什么时候出？", false, false, true)
    )
    override fun getCategories() = listOf(
        NotificationCategory("group", "粉丝", true),
        NotificationCategory("favorite", "赞", true),
        NotificationCategory("alternate_email", "@我的"),
        NotificationCategory("chat_bubble", "评论", badgeText = "9+")
    )
}
