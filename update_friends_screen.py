import re

with open('DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/FriendsScreen.kt', 'r') as f:
    content = f.read()

# Update VideoPage usage in FriendsScreen
new_video_page_usage = """
                // 由于 FriendsScreen 用的是 mock 数据（String），我们需要在这里适配或者创建一个临时的 VideoDto
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

content = re.sub(r'VideoPage\(\s*url\s*=\s*url,\s*isVisible\s*=\s*isVisible\s*\)', new_video_page_usage, content)

with open('DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/FriendsScreen.kt', 'w') as f:
    f.write(content)
