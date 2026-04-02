import sys

file_path = 'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt'

with open(file_path, 'r') as f:
    content = f.read()

# Add Share Sheet and Long Press logic
new_state_vars = """
        var showCommentsSheet by remember { mutableStateOf(false) }
        var showShareSheet by remember { mutableStateOf(false) }
        var showLongPressMenu by remember { mutableStateOf(false) }
"""

content = content.replace('        var showCommentsSheet by remember { mutableStateOf(false) }', new_state_vars)

# Update RightSideActions call
content = content.replace(
    'onCommentClick = { showCommentsSheet = true },',
    'onCommentClick = { showCommentsSheet = true }, onShareClick = { showShareSheet = true },'
)

# Add BottomSheet and Menu UI
new_sheets = """
        if (showCommentsSheet) {
            CommentsBottomSheet(onDismiss = { showCommentsSheet = false })
        }

        if (showShareSheet) {
            ShareBottomSheet(onDismiss = { showShareSheet = false })
        }

        if (showLongPressMenu) {
            LongPressMenu(onDismiss = { showLongPressMenu = false })
        }
"""
content = content.replace('        if (showCommentsSheet) {\n            CommentsBottomSheet(onDismiss = { showCommentsSheet = false })\n        }', new_sheets)

# Update pointerInput for Long Press
old_pointer_input = """            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        hearts.add(LikeHeart(x = offset.x, y = offset.y))
                    }
                )
            }"""

new_pointer_input = """            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        hearts.add(LikeHeart(x = offset.x, y = offset.y))
                    },
                    onLongPress = {
                        showLongPressMenu = true
                    }
                )
            }"""

content = content.replace(old_pointer_input, new_pointer_input)

# Add new composables at the end of file (before last bracket if any, but we'll append)
# Find the last closing brace and insert before it or just append
new_composables = """
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF161823),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                "分享到",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                val shares = listOf("私信", "群聊", "朋友圈", "微信", "QQ", "复制链接")
                items(shares) { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFF2E2E2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LongPressMenu(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .background(Color(0xFF2E2E2E), RoundedCornerShape(16.dp))
                .padding(vertical = 8.dp)
        ) {
            val options = listOf("不感兴趣", "保存视频", "收藏", "举报")
            options.forEach { option ->
                Text(
                    text = option,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
                if (option != options.last()) {
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 0.5.dp)
                }
            }
        }
    }
}
"""

with open(file_path, 'w') as f:
    f.write(content + new_composables)
