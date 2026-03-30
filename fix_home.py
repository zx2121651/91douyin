import sys

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt"
with open(file_path, 'r') as f:
    content = f.read()

search_str = """    Box(modifier = Modifier.fillMaxSize()) {
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
}"""

replace_str = """    val horizontalPagerState = rememberPagerState(initialPage = 2, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> {
                    // 同城骨架
                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                        Text("同城占位内容", color = Color.White, modifier = Modifier.align(Alignment.Center))
                    }
                }
                1 -> {
                    // 关注骨架
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        Text("关注页占位内容", color = Color.White, modifier = Modifier.align(Alignment.Center))
                    }
                }
                2 -> {
                    // 推荐视频流
                    VerticalPager(
                        state = pagerState,
                        beyondBoundsPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { vPage ->
                        val isVisible = pagerState.currentPage == vPage && horizontalPagerState.currentPage == 2
                        VideoPage(
                            url = videos[vPage],
                            isVisible = isVisible
                        )
                    }
                }
            }
        }

        TopNavigationBar(
            selectedTabIndex = horizontalPagerState.currentPage,
            onTabSelected = { index ->
                coroutineScope.launch {
                    horizontalPagerState.animateScrollToPage(index)
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )
    }
}"""

if search_str in content:
    content = content.replace(search_str, replace_str)
else:
    print("Could not find main pager replacement string")


nav_search_str = """@Composable
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

nav_replace_str = """@Composable
fun TopNavigationBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("同城", "关注", "推荐")

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
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTabIndex
                val alpha by animateFloatAsState(targetValue = if (isSelected) 1f else 0.7f, label = "alpha")
                val fontSize = if (isSelected) MaterialTheme.typography.titleLarge.fontSize else MaterialTheme.typography.titleMedium.fontSize
                val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onTabSelected(index) }
                ) {
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = alpha),
                        fontSize = fontSize,
                        fontWeight = fontWeight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(3.dp)
                                .background(Color.White, shape = CircleShape)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }
        }

        // Placeholder to balance the row since search is on the left
        Box(modifier = Modifier.size(28.dp))
    }
}"""

if nav_search_str in content:
    content = content.replace(nav_search_str, nav_replace_str)
else:
    print("Could not find nav replacement string")

with open(file_path, 'w') as f:
    f.write(content)
print("Finished python replacement.")
