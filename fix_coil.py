import sys

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt"
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace("import coil.compose.AsyncImage\n", "")

with open(file_path, 'w') as f:
    f.write(content)
