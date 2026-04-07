import re

with open('DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt', 'r') as f:
    content = f.read()

# Add imports for DoubleTapHeart Animation
content = content.replace("import com.app.douyin.pro.feature.home.ui.components.*", "import com.app.douyin.pro.feature.home.ui.components.*\nimport androidx.compose.ui.geometry.Offset\nimport kotlin.random.Random")

# Update VideoPage to handle heart states and gestures
new_video_page_logic = """
@Composable
fun VideoPage(video: VideoDto, isVisible: Boolean, onToggleFavorite: (Long) -> Unit) {
    var showCommentsSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showLongPressMenu by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    // 存储当前正在显示的红心列表
    val heartStates = remember { mutableStateListOf<HeartAnimationState>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isPaused = !isPaused },
                    onDoubleTap = { offset ->
                        // 添加一个新的红心状态
                        val rotation = Random.nextInt(-30, 30).toFloat()
                        heartStates.add(HeartAnimationState(id = System.currentTimeMillis(), offset = offset, rotation = rotation))

                        // 触发点赞业务逻辑 (如果当前未点赞，则点赞；如果已点赞，在抖音里双击通常不再取消点赞，我们这里简化为强制触发或仅当未点赞时触发)
                        if (!video.is_favorite) {
                            onToggleFavorite(video.id)
                        }
                    },
                    onLongPress = { showLongPressMenu = true }
                )
            }
    ) {
        VideoPlayerComponent(url = video.play_url, isVisible = isVisible, isDucked = showCommentsSheet, isPaused = isPaused)

        // 渲染双击产生的飘心动画
        DoubleTapHeartAnimation(
            heartStates = heartStates,
            onAnimationEnd = { id ->
                // 动画结束后移除
                heartStates.removeAll { it.id == id }
            }
        )

        ActionPanel(
            isLiked = video.is_favorite,
            likeCount = formatCount(video.favorite_count),
            commentCount = formatCount(video.comment_count),
            shareCount = "1.2k",
            onLikeClick = { onToggleFavorite(video.id) },
            onCommentClick = { showCommentsSheet = true },
            onShareClick = { showShareSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd),
            avatarUrl = video.author.avatar
        )

        VideoOverlay(
            author = video.author.name,
            description = video.title,
            musicTitle = "原声 - 潮流音乐库"
        )

        if (showCommentsSheet) CommentsBottomSheet(onDismiss = { showCommentsSheet = false })
        if (showShareSheet) ShareBottomSheet(onDismiss = { showShareSheet = false })
        if (showLongPressMenu) LongPressMenu(onDismiss = { showLongPressMenu = false })
    }
}
"""

# Replace the existing VideoPage
content = re.sub(r'@Composable\nfun VideoPage\(video: VideoDto, isVisible: Boolean, onToggleFavorite: \(Long\) -> Unit\) \{.*?(?=@Composable\nfun TopNavigationBar)', new_video_page_logic + '\n\n', content, flags=re.DOTALL)

with open('DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt', 'w') as f:
    f.write(content)
