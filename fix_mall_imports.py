import os

file_path = 'DouyinLite/feature_mall/src/main/java/com/app/douyin/pro/feature/mall/ui/MallScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

if 'import androidx.compose.material3.CardDefaults' not in content:
    content = content.replace('import androidx.compose.material3.Card', 'import androidx.compose.material3.Card\nimport androidx.compose.material3.CardDefaults')

with open(file_path, 'w') as f:
    f.write(content)
