import os

file_path = 'DouyinLite/feature_record/src/main/java/com/app/douyin/pro/feature/record/ui/RecordScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Fix imports
content = content.replace('import androidx.compose.ui.unit.dp', 'import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp')
content = content.replace('import androidx.compose.material.icons.filled.Info', 'import androidx.compose.material.icons.filled.Info\nimport androidx.compose.material.icons.filled.Timer\nimport androidx.compose.material.icons.filled.Cameraswitch\nimport androidx.compose.material.icons.filled.FiberManualRecord\nimport androidx.compose.material.icons.filled.FlipCameraAndroid')

# Redundant manual prefixing removal
content = content.replace('androidx.compose.material.icons.Icons.Filled.Timer', 'Icons.Filled.Timer')

with open(file_path, 'w') as f:
    f.write(content)
