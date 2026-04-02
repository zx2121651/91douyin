import os

file_path = 'DouyinLite/feature_inbox/src/main/java/com/app/douyin/pro/feature/inbox/ui/InboxScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Add missing UI imports
missing_imports = """
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Add
"""
content = content.replace('import androidx.compose.material.icons.filled.Check', 'import androidx.compose.material.icons.filled.Check' + missing_imports)

with open(file_path, 'w') as f:
    f.write(content)
