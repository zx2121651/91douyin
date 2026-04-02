import os

file_path = 'DouyinLite/feature_record/src/main/java/com/app/douyin/pro/feature/record/ui/RecordScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Only keep necessary imports, avoid double imports or wrong ones
lines = content.split('\n')
new_lines = []
for line in lines:
    if "import androidx.compose.material.icons.filled." in line:
        if "Close" in line or "Info" in line or "Refresh" in line or "PlayArrow" in line or "Check" in line:
             new_lines.append(line)
        continue
    new_lines.append(line)

# Add generic Icons imports
new_lines.insert(20, "import androidx.compose.material.icons.filled.*")

content = "\n".join(new_lines)
# Replace all usage with safe alternatives
content = content.replace("Icons.Filled.FlipCameraAndroid", "Icons.Default.Refresh")
content = content.replace("Icons.Filled.Notifications", "Icons.Default.Notifications")
content = content.replace("Icons.Filled.Circle", "Icons.Default.Check")

with open(file_path, 'w') as f:
    f.write(content)
