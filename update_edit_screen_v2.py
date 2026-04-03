import os

file_path = 'DouyinLite/feature_edit/src/main/java/com/app/douyin/pro/feature/edit/ui/screen/EditScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Update onNext call to exportProject
content = content.replace('onNext = onNext', 'onNext = { viewModel.exportProject { onNext() } }')

# 2. Add Export Overlay to EditScreen
export_overlay = """
    if (uiState.isExporting) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(progress = uiState.exportProgress / 100f, color = Color(0xFFFF2C55))
                Spacer(modifier = Modifier.height(16.dp))
                Text("正在导出 ${uiState.exportProgress}%", color = Color.White)
            }
        }
    }
"""

content = content.replace('containerColor = Color(0xFF161823)\n    ) { padding ->', 'containerColor = Color(0xFF161823)\n    ) { padding ->\n' + export_overlay)

# 3. Update TopBar parameter in EditScreen
content = content.replace('TopBar(\n                canUndo = uiState.canUndo,', 'TopBar(\n                isExporting = uiState.isExporting,\n                canUndo = uiState.canUndo,')

# 4. Update TopBar definition
content = content.replace('fun TopBar(\n    canUndo: Boolean,', 'fun TopBar(\n    isExporting: Boolean,\n    canUndo: Boolean,')

content = content.replace('Button(\n            onClick = onNext,', 'Button(\n            onClick = onNext,\n            enabled = !isExporting,')

with open(file_path, 'w') as f:
    f.write(content)
