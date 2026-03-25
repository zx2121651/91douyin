import sys

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt"
with open(file_path, 'r') as f:
    content = f.read()

# ADD IMPORTS
sed_script = """sed -i '/import androidx.compose.ui.Modifier/a \\
import androidx.compose.material3.OutlinedTextField\\
import androidx.compose.material3.TextFieldDefaults\\
import androidx.compose.foundation.layout.imePadding\\
import androidx.compose.material3.HorizontalDivider' DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt
"""

start_idx = content.find("fun CommentsBottomSheet(onDismiss: () -> Unit) {")
if start_idx != -1:
    end_idx = content.find("fun CommentsBottomSheet", start_idx + 10)
    if end_idx == -1: # No other function found, it's the last one
        end_idx = len(content)

    annot_idx = content.rfind("@Composable", 0, start_idx)
    # let's be safe and manually replace the old one with our new one exactly.

    comment_replace = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
        },
        containerColor = Color(0x99000000), // Glassmorphism translucent dark
        scrimColor = Color.Transparent, // Avoid completely darkening the video behind
        modifier = Modifier
            .fillMaxHeight(0.7f)
            .imePadding() // Pushes up when the keyboard opens
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "128 条评论",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Color.LightGray)
                }
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(20) { index ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "User $index", color = Color(0xFFC0C0C0), style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "This is a wonderful video! Really love the content here.", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("留下你的精彩评论...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.8f),
                        unfocusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.8f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
"""

    old_vp = """        // Video Player Layer
        VideoPlayer(url = url, isVisible = isVisible)"""

    vpage_search = """        var showCommentsSheet by remember { mutableStateOf(false) }

        // UI Layer
        RightSideActions("""

    vpage_replace = """        var showCommentsSheet by remember { mutableStateOf(false) }

        // Video Player Layer
        VideoPlayer(url = url, isVisible = isVisible, isDucked = showCommentsSheet)

        // UI Layer
        RightSideActions("""

    vp_search = """fun VideoPlayer(url: String, isVisible: Boolean) {"""
    vp_replace = """fun VideoPlayer(url: String, isVisible: Boolean, isDucked: Boolean = false) {"""

    vol_search = """    LaunchedEffect(isVisible) {"""
    vol_replace = """    LaunchedEffect(isDucked) {
        if (isDucked) {
            player.volume = 0.3f
        } else {
            player.volume = 1.0f
        }
    }

    LaunchedEffect(isVisible) {"""

    content = content[:annot_idx] + comment_replace
    content = content.replace(vp_search, vp_replace)
    content = content.replace(vol_search, vol_replace)
    content = content.replace(vpage_search, vpage_replace)
    content = content.replace(old_vp, "")

    with open(file_path, 'w') as f:
        f.write(content)
    print("Success replacing comments by slicing")
else:
    print("Could not find start of CommentsBottomSheet")
