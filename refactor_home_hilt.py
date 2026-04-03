import os

file_path = 'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Add imports
new_imports = """
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.douyin.pro.feature.home.viewmodel.HomeViewModel
import androidx.compose.runtime.collectAsState
"""
content = content.replace('import androidx.compose.ui.layout.ContentScale', 'import androidx.compose.ui.layout.ContentScale' + new_imports)

# 2. Update HomeScreen to use ViewModel
old_body = """@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun HomeScreen(onNavigateToMall: () -> Unit = {}, onNavigateToProfile: () -> Unit = {}) {
    val initialVideos = remember { MockData.videos }
    val videos = remember { mutableStateListOf<String>().apply { addAll(initialVideos) } }
    var isLoading by remember { mutableStateOf(false) }
    var pageCount by remember { mutableIntStateOf(1) }"""

new_body = """@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun HomeScreen(
    onNavigateToMall: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val videos = viewModel.videos
    val isLoading by viewModel.isLoading.collectAsState()"""

content = content.replace(old_body, new_body)

# 3. Update pagination logic in LaunchedEffect
old_pagination = """    // Pagination logic
    LaunchedEffect(pagerState.currentPage) {
        // Load more when reaching the 2nd to last item
        if (pagerState.currentPage >= videos.size - 2 && !isLoading) {
            isLoading = true
            val newVideos = MockData.loadMoreVideos(pageCount)
            videos.addAll(newVideos)
            pageCount++
            isLoading = false
        }
    }"""

new_pagination = """    // Pagination logic
    LaunchedEffect(pagerState.currentPage, videos.size) {
        // Load more when reaching the 2nd to last item
        if (videos.isNotEmpty() && pagerState.currentPage >= videos.size - 2 && !isLoading) {
            viewModel.loadMore()
        }
    }"""

content = content.replace(old_pagination, new_pagination)

with open(file_path, 'w') as f:
    f.write(content)
