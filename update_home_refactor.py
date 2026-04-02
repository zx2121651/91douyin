import os

file_path = 'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Define VideoFeed component
video_feed_comp = """
@Composable
fun VideoFeed(
    videos: List<String>,
    isVisible: Boolean,
    onNavigateToProfile: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { videos.size })

    VerticalPager(
        state = pagerState,
        beyondBoundsPageCount = 1,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    if (dragAmount < -30f) {
                        onNavigateToProfile()
                    }
                }
            }
    ) { vPage ->
        VideoPage(
            url = videos[vPage],
            isVisible = isVisible && pagerState.currentPage == vPage
        )
    }
}
"""

# Append at the end of file (before LongPressMenu or anywhere)
content = content.replace('@Composable\nfun LongPressMenu', video_feed_comp + '\n@Composable\nfun LongPressMenu')

# 2. Update HomeScreen to use VideoFeed
# Replacing tab 2 (Follow) and tab 3 (Recommend)
old_follow = """                2 -> {
                    // 关注页视频流
                    val followPagerState = rememberPagerState(pageCount = { videos.size })
                    VerticalPager(
                        state = followPagerState,
                        beyondBoundsPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { vPage ->
                        VideoPage(
                            url = videos[(vPage + 2) % videos.size], // Different offset
                            isVisible = pagerState.currentPage == vPage && horizontalPagerState.currentPage == 2
                        )
                    }
                }"""

new_follow = """                2 -> {
                    VideoFeed(
                        videos = videos.reversed(),
                        isVisible = horizontalPagerState.currentPage == 2,
                        onNavigateToProfile = onNavigateToProfile
                    )
                }"""

old_recommend = """                3 -> {
                    // 推荐视频流
                    VerticalPager(
                        state = pagerState,
                        beyondBoundsPageCount = 1,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { change, dragAmount ->
                                    if (dragAmount < -30f) { // Left swipe
                                        onNavigateToProfile()
                                    }
                                }
                            }
                    ) { vPage ->
                        val isVisible = pagerState.currentPage == vPage && horizontalPagerState.currentPage == 3
                        VideoPage(
                            url = videos[vPage],
                            isVisible = isVisible
                        )
                    }
                }"""

new_recommend = """                3 -> {
                    VideoFeed(
                        videos = videos,
                        isVisible = horizontalPagerState.currentPage == 3,
                        onNavigateToProfile = onNavigateToProfile
                    )
                }"""

content = content.replace(old_follow, new_follow)
content = content.replace(old_recommend, new_recommend)

with open(file_path, 'w') as f:
    f.write(content)
