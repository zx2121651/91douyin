import os

file_path = 'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Implement Follow feed in Page 2
follow_feed_code = """                2 -> {
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

content = content.replace('                2 -> {\n                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {\n                        Text("关注页占位内容", color = Color.White, modifier = Modifier.align(Alignment.Center))\n                    }\n                }', follow_feed_code)

with open(file_path, 'w') as f:
    f.write(content)
