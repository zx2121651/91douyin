import os

file_path = 'DouyinLite/feature_record/src/main/java/com/app/douyin/pro/feature/record/ui/RecordScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Add Timer State
content = content.replace(
    'var previewTexture by remember { mutableStateOf<SurfaceTexture?>(null) }',
    'var previewTexture by remember { mutableStateOf<SurfaceTexture?>(null) }\n    var countdownTime by remember { mutableIntStateOf(0) }'
)

# 2. Add Timer Side Icon
timer_icon = """
            IconButton(onClick = { countdownTime = 3 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(androidx.compose.material.icons.Icons.Filled.Timer, contentDescription = "Timer", tint = Color.White)
                    Text("3s", color = Color.White, fontSize = 10.sp)
                }
            }
"""
content = content.replace(
    'IconButton(onClick = { }) { Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White) }',
    'IconButton(onClick = { }) { Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White) }\n' + timer_icon
)

# 3. Add Countdown Logic
countdown_logic = """
    LaunchedEffect(countdownTime) {
        if (countdownTime > 0) {
            delay(1000)
            countdownTime -= 1
            if (countdownTime == 0) {
                // Trigger recording start if not already recording
                if (!isRecording) {
                    // Logic to trigger fab click simulation or direct start
                }
            }
        }
    }
"""
# Note: I need to import delay
if 'import kotlinx.coroutines.delay' not in content:
    content = content.replace('import androidx.compose.runtime.setValue', 'import androidx.compose.runtime.setValue\nimport kotlinx.coroutines.delay')

# 4. Add Countdown Overlay
countdown_overlay = """
        if (countdownTime > 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = countdownTime.toString(),
                    color = Color.White,
                    fontSize = 120.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
"""
content = content.replace('Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {', 'Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {\n' + countdown_overlay)

with open(file_path, 'w') as f:
    f.write(content)
