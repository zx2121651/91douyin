import sys

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt"
with open(file_path, 'r') as f:
    content = f.read()

import_str = """
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
"""
content = content.replace("import androidx.compose.material.icons.filled.PlayArrow", "import androidx.compose.material.icons.filled.PlayArrow" + import_str)

search_str = """    VerticalPager(
        state = pagerState,
        beyondBoundsPageCount = 1,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val isVisible = pagerState.currentPage == page
        VideoPage(
            url = videos[page],
            isVisible = isVisible
        )
    }
}"""

replace_str = """    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            beyondBoundsPageCount = 1,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val isVisible = pagerState.currentPage == page
            VideoPage(
                url = videos[page],
                isVisible = isVisible
            )
        }

        TopNavigationBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )
    }
}

@Composable
fun TopNavigationBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "同城", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal)
            Text(text = "关注", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "推荐", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(3.dp)
                        .background(Color.White, shape = CircleShape)
                )
            }
        }

        // Placeholder to balance the row since search is on the left
        Box(modifier = Modifier.size(28.dp))
    }
}"""

if search_str in content:
    content = content.replace(search_str, replace_str)
    with open(file_path, 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Could not find replacement string")
