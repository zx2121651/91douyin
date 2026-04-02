import os

file_path = 'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt'
with open(file_path, 'r') as f:
    lines = f.readlines()

new_imports = [
    "import androidx.compose.foundation.lazy.LazyRow\n",
    "import androidx.compose.foundation.lazy.items\n",
]

# Insert imports at the top
for imp in new_imports:
    if imp not in lines:
        lines.insert(3, imp)

# Fix showLongPressMenu scope issue
content = "".join(lines)

# Re-read and re-apply state vars to ensure they are inside VideoPage
# I probably replaced it outside the scope or something.
# Let's check where it is.

with open(file_path, 'w') as f:
    f.write(content)
