import re

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

pattern = re.compile(r"(@OptIn\(ExperimentalMaterial3Api::class\)\n)?@Composable\nfun CommentsBottomSheet.*?\}\n\}\n", re.DOTALL)
content = pattern.sub("", content, count=1)

with open(file_path, "w") as f:
    f.write(content)
