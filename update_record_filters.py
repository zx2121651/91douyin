import os

file_path = 'DouyinLite/feature_record/src/main/java/com/app/douyin/pro/feature/record/ui/RecordScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Add Filter State
content = content.replace(
    'var countdownTime by remember { mutableIntStateOf(0) }',
    'var countdownTime by remember { mutableIntStateOf(0) }\n    var showFilters by remember { mutableStateOf(false) }'
)

# Add Filter Button
filter_button = """
            IconButton(onClick = { showFilters = true }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Face, contentDescription = "Filters", tint = Color.White)
                    Text("滤镜", color = Color.White, fontSize = 10.sp)
                }
            }
"""
content = content.replace(
    'IconButton(onClick = { }) { Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White) }',
    'IconButton(onClick = { }) { Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White) }\n' + filter_button
)

# Add Filter Selection Overlay at the bottom
filter_overlay = """
        if (showFilters) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(180.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showFilters = false }
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("选择滤镜", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val filters = listOf("原图", "磨皮", "冷白", "复古", "胶片", "黑白")
                        items(filters) { filter ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (filter == "磨皮") Color(0xFFFF2C55) else Color.DarkGray)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(filter, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
"""
# Note: I need to import LazyRow and items
if 'import androidx.compose.foundation.lazy.LazyRow' not in content:
    content = content.replace('import androidx.compose.foundation.layout.Row', 'import androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.lazy.LazyRow\nimport androidx.compose.foundation.lazy.items')

content = content.replace('FloatingActionButton(', filter_overlay + '\n\n        FloatingActionButton(')

with open(file_path, 'w') as f:
    f.write(content)
