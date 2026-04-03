import os

file_path = 'DouyinLite/feature_edit/src/main/java/com/app/douyin/pro/feature/edit/ui/screen/EditScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Update TimelineArea call in EditScreen
content = content.replace(
    'TimelineArea(\n                tracks = uiState.tracks,\n                currentTimeMs = uiState.currentTimeMs,\n                totalDurationMs = uiState.totalDurationMs\n            )',
    'TimelineArea(\n                tracks = uiState.tracks,\n                currentTimeMs = uiState.currentTimeMs,\n                totalDurationMs = uiState.totalDurationMs,\n                onSeek = viewModel::updateCurrentTime,\n                onSelectClip = viewModel::selectClip\n            )'
)

with open(file_path, 'w') as f:
    f.write(content)
