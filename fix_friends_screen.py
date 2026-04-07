import re

with open('DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/FriendsScreen.kt', 'r') as f:
    content = f.read()

# Replace the wrong call completely
wrong_call = """VideoPage(
                    url = url,
                    isVisible = isVisible
                )"""

correct_call = """
                val tempVideo = com.app.douyin.pro.lib.media.network.VideoDto(
                    id = 0,
                    play_url = url,
                    cover_url = "",
                    favorite_count = 0,
                    comment_count = 0,
                    is_favorite = false,
                    title = "朋友的视频",
                    author = com.app.douyin.pro.lib.media.network.UserDto(0, "好友", "", false)
                )
                VideoPage(
                    video = tempVideo,
                    isVisible = isVisible,
                    onToggleFavorite = { /* 朋友页暂时不处理点赞 */ }
                )
"""

content = content.replace(wrong_call, correct_call)

with open('DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/FriendsScreen.kt', 'w') as f:
    f.write(content)
