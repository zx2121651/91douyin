import os

file_path = 'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Fix VideoPage states and single tap
content = content.replace(
    'var showLongPressMenu by remember { mutableStateOf(false) }',
    'var showLongPressMenu by remember { mutableStateOf(false) }\n    var isPaused by remember { mutableStateOf(false) }'
)

old_detect = """                detectTapGestures(
                    onDoubleTap = { offset ->
                        hearts.add(LikeHeart(x = offset.x, y = offset.y))
                    },
                    onLongPress = {
                        showLongPressMenu = true
                    }
                )"""

new_detect = """                detectTapGestures(
                    onTap = {
                        isPaused = !isPaused
                    },
                    onDoubleTap = { offset ->
                        hearts.add(LikeHeart(x = offset.x, y = offset.y))
                    },
                    onLongPress = {
                        showLongPressMenu = true
                    }
                )"""
content = content.replace(old_detect, new_detect)

# Update VideoPlayer call and add overlay
content = content.replace(
    'VideoPlayer(url = url, isVisible = isVisible, isDucked = showCommentsSheet)',
    'VideoPlayer(url = url, isVisible = isVisible, isDucked = showCommentsSheet, isPaused = isPaused)\n\n        androidx.compose.animation.AnimatedVisibility(\n            visible = isPaused,\n            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(initialScale = 1.5f),\n            \x65xit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(targetScale = 1.5f),\n            modifier = Modifier.align(Alignment.Center)\n        ) {\n            Icon(\n                imageVector = Icons.Filled.PlayArrow,\n                contentDescription = "Paused",\n                tint = Color.White.copy(alpha = 0.5f),\n                modifier = Modifier.size(80.dp)\n            )\n        }'
)

# Update VideoPlayer function signature and logic
content = content.replace(
    'fun VideoPlayer(url: String, isVisible: Boolean, isDucked: Boolean = false) {',
    'fun VideoPlayer(url: String, isVisible: Boolean, isDucked: Boolean = false, isPaused: Boolean = false) {'
)

old_player_logic = """    LaunchedEffect(isVisible, isDucked) {
        if (isVisible && !isDucked) {
            player.play()
        } else {
            player.pause()
        }
    }"""

new_player_logic = """    LaunchedEffect(isVisible, isDucked, isPaused) {
        if (isVisible && !isDucked && !isPaused) {
            player.play()
        } else {
            player.pause()
        }
    }"""
content = content.replace(old_player_logic, new_player_logic)

with open(file_path, 'w') as f:
    f.write(content)
