import os

file_path = 'DouyinLite/feature_inbox/src/main/java/com/app/douyin/pro/feature/inbox/ui/InboxScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Add Chat Screen logic
chat_screen_composable = """
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(name: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkSurface,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceContainer)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("发送消息...", color = TextSecondary, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(androidx.compose.material.icons.Icons.Default.Face, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = null, tint = TextPrimary)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Mock Message
            Row(verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                        .background(DarkSurfaceContainer)
                        .padding(12.dp)
                ) {
                    Text("你好呀！最近有看到我发的视频吗？", color = TextPrimary, fontSize = 15.sp)
                }
            }
        }
    }
}
"""

content = content + "\n" + chat_screen_composable

with open(file_path, 'w') as f:
    f.write(content)
