import re

with open('DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt', 'r') as f:
    content = f.read()

# Fix VideoFeed imports
content = content.replace("import com.app.douyin.pro.feature.home.viewmodel.HomeViewModel", "import com.app.douyin.pro.feature.home.viewmodel.HomeViewModel\nimport com.app.douyin.pro.lib.media.network.VideoDto")

# Fix VideoFeed usages
content = content.replace("2 -> VideoFeed(videos = videos.reversed(), isVisible = horizontalPagerState.currentPage == 2, onNavigateToProfile = onNavigateToProfile)", "2 -> VideoFeed(videos = videos.reversed(), isVisible = horizontalPagerState.currentPage == 2, onNavigateToProfile = onNavigateToProfile, onToggleFavorite = { viewModel.toggleFavorite(it) })")
content = content.replace("3 -> VideoFeed(videos = videos, isVisible = horizontalPagerState.currentPage == 3, onNavigateToProfile = onNavigateToProfile)", "3 -> VideoFeed(videos = videos, isVisible = horizontalPagerState.currentPage == 3, onNavigateToProfile = onNavigateToProfile, onToggleFavorite = { viewModel.toggleFavorite(it) })")

# Fix VideoPage signature
content = content.replace("fun VideoPage(url: String, isVisible: Boolean) {", "fun VideoPage(video: VideoDto, isVisible: Boolean, onToggleFavorite: (Long) -> Unit) {")

# Fix VideoPlayerComponent url
content = content.replace("VideoPlayerComponent(url = url, isVisible = isVisible, isDucked = showCommentsSheet, isPaused = isPaused)", "VideoPlayerComponent(url = video.play_url, isVisible = isVisible, isDucked = showCommentsSheet, isPaused = isPaused)")

# Fix ActionPanel variables
content = content.replace("""        ActionPanel(
            isLiked = false,
            likeCount = "12.5w",
            commentCount = "856",
            shareCount = "1.2k",
            onLikeClick = { },
            onCommentClick = { showCommentsSheet = true },
            onShareClick = { showShareSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        )""", """        ActionPanel(
            isLiked = video.is_favorite,
            likeCount = formatCount(video.favorite_count),
            commentCount = formatCount(video.comment_count),
            shareCount = "1.2k",
            onLikeClick = { onToggleFavorite(video.id) },
            onCommentClick = { showCommentsSheet = true },
            onShareClick = { showShareSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd),
            avatarUrl = video.author.avatar
        )""")

content = content.replace("""        VideoOverlay(
            author = "潮流先锋",
            description = "这是一段非常精彩的视频描述 #抖音 #Compose",
            musicTitle = "原声 - 潮流音乐库"
        )""", """        VideoOverlay(
            author = video.author.name,
            description = video.title,
            musicTitle = "原声 - 潮流音乐库"
        )""")

# Add formatCount helper
content += """
fun formatCount(count: Long): String {
    return if (count >= 10000) {
        String.format("%.1fw", count / 10000.0)
    } else {
        count.toString()
    }
}
"""

with open('DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt', 'w') as f:
    f.write(content)
