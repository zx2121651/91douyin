import os

file_path = 'DouyinLite/feature_record/src/main/java/com/app/douyin/pro/feature/record/ui/RecordScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Add missing UI imports
missing_imports = """
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Face
"""
content = content.replace('import androidx.compose.ui.unit.sp', 'import androidx.compose.ui.unit.sp' + missing_imports)

with open(file_path, 'w') as f:
    f.write(content)
