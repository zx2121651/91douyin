import sys

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt"
with open(file_path, 'r') as f:
    content = f.read()

# Adding Lifecycle imports and golden color
sed_script = """sed -i '/import androidx.compose.ui.Modifier/a \\
import androidx.lifecycle.LifecycleEventObserver\\
import androidx.compose.ui.platform.LocalLifecycleOwner\\
import androidx.lifecycle.Lifecycle\\
import kotlinx.coroutines.isActive\\
import kotlinx.coroutines.delay' DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt
"""

search_str = """@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun HomeScreen() {
    val videos = MockData.videos

    // Using BeyondBoundsPageCount = 1 to pre-load adjacent pages for smoother scrolling
    val pagerState = rememberPagerState(pageCount = { videos.size })"""

replace_str = """@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun HomeScreen() {
    val initialVideos = remember { MockData.videos }
    val videos = remember { mutableStateListOf<String>().apply { addAll(initialVideos) } }
    var isLoading by remember { mutableStateOf(false) }
    var pageCount by remember { mutableIntStateOf(1) }

    // Using BeyondBoundsPageCount = 1 to pre-load adjacent pages for smoother scrolling
    val pagerState = rememberPagerState(pageCount = { videos.size })

    // Pagination logic
    LaunchedEffect(pagerState.currentPage) {
        // Load more when reaching the 2nd to last item
        if (pagerState.currentPage >= videos.size - 2 && !isLoading) {
            isLoading = true
            val newVideos = MockData.loadMoreVideos(pageCount)
            videos.addAll(newVideos)
            pageCount++
            isLoading = false
        }
    }
"""

if search_str in content:
    content = content.replace(search_str, replace_str)

    # Also update the heart color to Bright Gold
    heart_search = "tint = if (isLiked) Color.Red else Color.White,"
    heart_replace = "tint = if (isLiked) Color(0xFFFFD700) else Color.White,"
    content = content.replace(heart_search, heart_replace)

    # And global audio focus handling:
    lifecycle_search = """    LaunchedEffect(isVisible) {
        if (isVisible) {
            player.play()
        } else {
            player.pause()
            player.seekTo(0) // Precise lifecycle hook: Reset to 0 when unselected
        }
    }"""
    lifecycle_replace = """    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_PAUSE) {
                player.pause()
            } else if (event == Lifecycle.Event.ON_RESUME && isVisible) {
                player.play()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            // EXOPlayer setAudioAttributes handles audio focus automatically internally
            val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()
            player.setAudioAttributes(audioAttributes, true)
            player.play()
        } else {
            player.pause()
            player.seekTo(0) // Precise lifecycle hook: Reset to 0 when unselected
        }
    }"""
    content = content.replace(lifecycle_search, lifecycle_replace)

    with open(file_path, 'w') as f:
        f.write(content)
    print("Pagination and Focus updated.")
else:
    print("Could not find string for pagination replace.")
