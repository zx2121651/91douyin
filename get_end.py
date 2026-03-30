import sys

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt"
with open(file_path, 'r') as f:
    content = f.read()

start_idx = content.find("fun CommentsBottomSheet")
print(content[start_idx:start_idx+2000])
